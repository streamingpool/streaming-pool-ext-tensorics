/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamfactory;

import static cern.streaming.pool.core.service.util.ReactiveStreams.fromRx;
import static cern.streaming.pool.core.service.util.ReactiveStreams.rxFrom;
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
import java.util.stream.Collectors;

import org.assertj.core.util.Objects;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.resolve.options.HandleWithFirstCapableAncestorStrategy;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;
import org.tensorics.core.tree.domain.ResolvingContext;
import org.tensorics.core.tree.walking.Trees;

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
import cern.streaming.pool.ext.tensorics.expression.StreamIdBasedExpression;
import cern.streaming.pool.ext.tensorics.streamid.DetailedExpressionStreamId;
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
            ExpToValue castedEntry = Objects.castIfBelongsToType(entry, ExpToValue.class);
            if (castedEntry != null) {
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

        Map<StreamIdBasedExpression<Object>, StreamId<Object>> streamIds = streamIdsFrom(expression);

        Map<StreamId<?>, Observable<ExpToValue>> observableEntries = new HashMap<>();
        for (Entry<StreamIdBasedExpression<Object>, StreamId<Object>> entry : streamIds.entrySet()) {
            Observable<?> plainObservable = rxFrom(discoveryService.discover(entry.getValue()));
            Observable<ExpToValue> mappedObservable = plainObservable.map(obj -> new ExpToValue(entry.getKey(), obj));
            observableEntries.put(entry.getValue(), mappedObservable);
        }

        return triggerObservable(observableEntries, id.evaluationStrategy(), discoveryService)
                .withLatestFrom(observableEntries.values().toArray(new Observable[] {}), CONTEXT_COMBINER)
                .map(ctx -> engine.resolveDetailed(expression, ctx, EXCEPTION_HANDLING_STRATEGY));
    }

    @SuppressWarnings("unchecked")
    private <E extends Expression<?>> Map<StreamIdBasedExpression<Object>, StreamId<Object>> streamIdsFrom(
            E expression) {
        Collection<Node> leaves = Trees.findBottomNodes(expression);
        return leaves.stream().filter(node -> node instanceof StreamIdBasedExpression)
                .map(node -> (StreamIdBasedExpression<Object>) node)
                .collect(Collectors.toMap(exp -> exp, exp -> exp.streamId()));
    }

    private static final Observable<?> triggerObservable(Map<StreamId<?>, ? extends Observable<?>> observables,
            EvaluationStrategy strategy, DiscoveryService discoveryService) {
        if (strategy instanceof ContinuousEvaluation) {
            return combineLatest(observables.values(), TRIGGER_CONTEXT_COMBINER);
        }
        if (strategy instanceof BufferedEvaluation) {
            List<? extends Observable<?>> triggeringObservables = observables.entrySet().stream()
                    .filter(e -> (e.getKey() instanceof OverlapBufferStreamId)).map(Entry::getValue).collect(toList());
            return zip(triggeringObservables, ImmutableSet::of);
        }
        if (strategy instanceof TriggeredEvaluation) {
            return rxFrom(discoveryService.discover(((TriggeredEvaluation) strategy).triggeringStreamId()));
        }
        throw new IllegalArgumentException(
                "Unknown evaluationStrategy '" + strategy + "'. Cannot create trigger Observable.");
    }

    private static final class ExpToValue {

        public ExpToValue(StreamIdBasedExpression<Object> node, Object value) {
            super();
            this.node = node;
            this.value = value;
        }

        private final StreamIdBasedExpression<Object> node;
        private final Object value;
    }

}
