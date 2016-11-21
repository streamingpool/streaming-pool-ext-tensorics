/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamfactory;

import static cern.streaming.pool.core.service.util.ReactiveStreams.fromRx;
import static cern.streaming.pool.core.service.util.ReactiveStreams.rxFrom;
import static java.lang.String.format;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static rx.Observable.combineLatest;
import static rx.Observable.zip;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.resolve.engine.ResolvedContextDidNotGrowException;
import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.resolve.options.HandleWithFirstCapableAncestorStrategy;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.ResolvingContext;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
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
import cern.streaming.pool.ext.tensorics.support.TensoricsTreeSupport;
import rx.Observable;
import rx.functions.FuncN;

/**
 * @author kfuchsbe, caguiler
 */
public class DetailedTensoricsExpressionStreamFactory implements StreamFactory {

    private static final HandleWithFirstCapableAncestorStrategy EXCEPTION_HANDLING_STRATEGY = new HandleWithFirstCapableAncestorStrategy();

    private static final FuncN<Boolean> TRIGGER_CONTEXT_COMBINER = (Object... entriesToCombine) -> {
        return true;
    };

    private static final FuncN<ResolvingContext> CONTEXT_COMBINER = (Object... entriesToCombine) -> {
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
    public <T> Optional<ReactiveStream<T>> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof DetailedExpressionStreamId)) {
            return Optional.empty();
        }
        return of((ReactiveStream<T>) fromRx(resolvedStream((DetailedExpressionStreamId<?, ?>) id, discoveryService)));
    }

    private <T, E extends Expression<T>> Observable<DetailedExpressionResult<T, E>> resolvedStream(
            DetailedExpressionStreamId<T, E> id, DiscoveryService discoveryService) {
        E expression = id.expression();
        Map<Expression<Object>, StreamId<Object>> streamIds = streamIdsFrom(id);
        Map<StreamId<?>, Observable<ExpToValue>> observableEntries = new HashMap<>();
        for (Entry<Expression<Object>, StreamId<Object>> entry : streamIds.entrySet()) {
            Observable<?> plainObservable = rxFrom(discoveryService.discover(entry.getValue()));
            Observable<ExpToValue> mappedObservable;
            mappedObservable = plainObservable.map(obj -> new ExpToValue(entry.getKey(), obj));
            observableEntries.put(entry.getValue(), mappedObservable);
        }

        return triggerObservable(observableEntries, id.evaluationStrategy(), discoveryService)
                .withLatestFrom(observableEntries.values().toArray(new Observable[] {}), CONTEXT_COMBINER)
                .map(ctx -> engine.resolveDetailed(expression, ctx, EXCEPTION_HANDLING_STRATEGY));
    }

    /* package visibility for testing */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    <T extends Expression<?>> Map<Expression<Object>, StreamId<Object>> streamIdsFrom(
            DetailedExpressionStreamId<?, T> id) {
        Expression<?> rootExpression = id.expression();
        ResolvingContext initialCtx = id.initialCtx();

        Collection<UnresolvedStreamIdBasedExpression> unresolvedStreamIdExpressions = TensoricsTreeSupport
                .getNodesOfClass(rootExpression, UnresolvedStreamIdBasedExpression.class);

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

    private static final Observable<?> triggerObservable(Map<StreamId<?>, ? extends Observable<?>> observables,
            EvaluationStrategy strategy, DiscoveryService discoveryService) {
        if (strategy instanceof ContinuousEvaluation) {
            return combineLatest(observables.values(), TRIGGER_CONTEXT_COMBINER);
        }
        if (strategy instanceof BufferedEvaluation) {
            List<? extends Observable<?>> triggeringObservables = observables.entrySet().stream()
                    .filter(e -> e.getKey() instanceof OverlapBufferStreamId).map(Entry::getValue).collect(toList());
            if (triggeringObservables.isEmpty()) {
                throw new NoBufferedStreamSpecifiedException();
            }
            return zip(triggeringObservables, ImmutableSet::of);
        }
        if (strategy instanceof TriggeredEvaluation) {
            return rxFrom(discoveryService.discover(((TriggeredEvaluation) strategy).triggeringStreamId()));
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
