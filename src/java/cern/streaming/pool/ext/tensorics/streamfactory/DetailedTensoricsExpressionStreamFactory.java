/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamfactory;

import static java.lang.String.format;
import static io.reactivex.Flowable.fromPublisher;
import static io.reactivex.Flowable.zip;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.resolve.engine.ResolvedContextDidNotGrowException;
import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.resolve.options.HandleWithFirstCapableAncestorStrategy;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.ResolvingContext;
import org.tensorics.core.tree.walking.Trees;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.streamid.OverlapBufferStreamId;
import cern.streaming.pool.ext.tensorics.evaluation.BufferedEvaluation;
import cern.streaming.pool.ext.tensorics.evaluation.ContinuousEvaluation;
import cern.streaming.pool.ext.tensorics.evaluation.EvaluationStrategy;
import cern.streaming.pool.ext.tensorics.evaluation.TriggeredEvaluation;
import cern.streaming.pool.ext.tensorics.exception.NoBufferedStreamSpecifiedException;
import cern.streaming.pool.ext.tensorics.expression.UnresolvedStreamIdBasedExpression;
import cern.streaming.pool.ext.tensorics.streamid.DetailedExpressionStreamId;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * @author kfuchsbe, caguiler
 */
public class DetailedTensoricsExpressionStreamFactory implements StreamFactory {

    private static final HandleWithFirstCapableAncestorStrategy EXCEPTION_HANDLING_STRATEGY = new HandleWithFirstCapableAncestorStrategy();

    private static final Function<Object[], Boolean> TRIGGER_CONTEXT_COMBINER = (Object... entriesToCombine) -> {
        return true;
    };

    private static final Function<Object[], ResolvingContext> CONTEXT_COMBINER = (Object... entriesToCombine) -> {
        EditableResolvingContext context = Contexts.newResolvingContext();
        for (Object entry : entriesToCombine) {
            if (entry instanceof ExpToValue) {
                ExpToValue castedEntry = (ExpToValue) entry;
                context.put(castedEntry.node, castedEntry.value);
            }
        }
        return context;
    };

    private final ResolvingEngine engine;

    public DetailedTensoricsExpressionStreamFactory(ResolvingEngine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<Publisher<T>> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof DetailedExpressionStreamId)) {
            return Optional.empty();
        }
        return of((Flowable<T>) resolvedStream((DetailedExpressionStreamId<?, ?>) id, discoveryService));
    }

    private <T, E extends Expression<T>> Flowable<DetailedExpressionResult<T, E>> resolvedStream(
            DetailedExpressionStreamId<T, E> id, DiscoveryService discoveryService) {
        E expression = id.expression();
        ResolvingContext initialCtx = id.initialCtx();

        Map<Expression<Object>, StreamId<Object>> streamIds = streamIdsFrom(id);
        Map<StreamId<?>, Flowable<ExpToValue>> observableEntries = new HashMap<>();
        for (Entry<Expression<Object>, StreamId<Object>> entry : streamIds.entrySet()) {
            Flowable<?> plainObservable = fromPublisher(discoveryService.discover(entry.getValue()));
            Flowable<ExpToValue> mappedObservable;
            mappedObservable = plainObservable.map(obj -> new ExpToValue(entry.getKey(), obj));
            observableEntries.put(entry.getValue(), mappedObservable);
        }

        return triggerObservable(observableEntries, id.evaluationStrategy(), discoveryService)
                .withLatestFrom(observableEntries.values().toArray(new Flowable[] {}), CONTEXT_COMBINER).map(ctx -> {
                    EditableResolvingContext fullContext = Contexts.newResolvingContext();
                    fullContext.putAllNew(ctx);
                    fullContext.putAllNew(initialCtx);
                    return engine.resolveDetailed(expression, fullContext, EXCEPTION_HANDLING_STRATEGY);
                });
    }

    /* package visibility for testing */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @VisibleForTesting
    <T extends Expression<?>> Map<Expression<Object>, StreamId<Object>> streamIdsFrom(
            DetailedExpressionStreamId<?, T> id) {
        Expression<?> rootExpression = id.expression();
        ResolvingContext initialCtx = id.initialCtx();

        Collection<UnresolvedStreamIdBasedExpression> unresolvedStreamIdExpressions = Trees
                .findNodesOfClass(rootExpression, UnresolvedStreamIdBasedExpression.class);

        Builder<Expression<Object>, StreamId<Object>> mapBuilder = ImmutableMap.builder();
        for (UnresolvedStreamIdBasedExpression<Object> unresolvedStreamIdExpression : unresolvedStreamIdExpressions) {
            try {
                Expression<StreamId<Object>> streamIdExpression = unresolvedStreamIdExpression.streamIdExpression();
                StreamId<Object> streamId = engine.resolve(streamIdExpression, initialCtx);
                mapBuilder.put(unresolvedStreamIdExpression, streamId);
            } catch (ResolvedContextDidNotGrowException ex) {
                throw new RuntimeException(format(
                        "Context did not grow while resolving the StreamId of expression. "
                                + "This is most probably because the initial context (%s) did not contain the value of the current UnresolvedStreamIdBasedExpression (%s).",
                        initialCtx, unresolvedStreamIdExpression), ex);
            }
        }
        return mapBuilder.build();
    }

    private static final Flowable<?> triggerObservable(Map<StreamId<?>, ? extends Flowable<?>> flowables,
            EvaluationStrategy strategy, DiscoveryService discoveryService) {
        if (strategy instanceof ContinuousEvaluation) {
            return Flowable.combineLatest(flowables.values(), TRIGGER_CONTEXT_COMBINER);
        }
        if (strategy instanceof BufferedEvaluation) {
            List<? extends Flowable<?>> triggeringObservables = flowables.entrySet().stream()
                    .filter(e -> (e.getKey() instanceof OverlapBufferStreamId)).map(Entry::getValue).collect(toList());
            if (triggeringObservables.isEmpty()) {
                throw new NoBufferedStreamSpecifiedException();
            }
            return zip(triggeringObservables, ImmutableSet::of);
        }
        if (strategy instanceof TriggeredEvaluation) {
            return fromPublisher(discoveryService.discover(((TriggeredEvaluation) strategy).triggeringStreamId()));
        }
        throw new IllegalArgumentException(
                "Unknown evaluationStrategy '" + strategy + "'. Cannot create trigger Observable.");
    }

    private static final class ExpToValue {

        public ExpToValue(Expression<Object> node, Object value) {
            super();
            this.node = node;
            this.value = value;
        }

        private final Expression<Object> node;
        private final Object value;
    }

}
