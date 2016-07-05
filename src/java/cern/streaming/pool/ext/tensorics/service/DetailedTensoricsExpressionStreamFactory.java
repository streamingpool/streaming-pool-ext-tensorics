/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static cern.streaming.pool.core.service.util.ReactiveStreams.fromRx;
import static cern.streaming.pool.core.service.util.ReactiveStreams.rxFrom;
import static rx.Observable.combineLatest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.tensorics.core.resolve.domain.DetailedResolvedExpression;
import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;
import org.tensorics.core.tree.domain.ResolvingContext;
import org.tensorics.core.tree.walking.Trees;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.domain.DetailedExpressionStreamId;
import cern.streaming.pool.ext.tensorics.domain.StreamIdBasedExpression;
import rx.Observable;
import rx.functions.FuncN;

/**
 * @author kfuchsbe, caguiler
 */
public class DetailedTensoricsExpressionStreamFactory implements StreamFactory {

    private static final FuncN<ResolvingContext> CONTEXT_COMBINER = (Object... entriesToCombine) -> {
        EditableResolvingContext context = Contexts.newResolvingContext();
        for (Object entry : entriesToCombine) {
            ExpToValue castedEntry = (ExpToValue) entry;
            context.put(castedEntry.node, castedEntry.value);
        }
        return context;
    };

    private final ResolvingEngine engine;

    public DetailedTensoricsExpressionStreamFactory(ResolvingEngine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ReactiveStream<T> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof DetailedExpressionStreamId)) {
            return null;
        }
        return (ReactiveStream<T>) fromRx(resolvedStream((DetailedExpressionStreamId<?, ?>) id, discoveryService));
    }

    private <T, E extends Expression<T>> Observable<DetailedResolvedExpression<T, E>> resolvedStream(
            DetailedExpressionStreamId<T, E> id, DiscoveryService discoveryService) {
        E expression = id.getExpression();

        Collection<Node> leaves = Trees.findBottomNodes(expression);

        System.out.println("leaves:" + leaves);
        
        @SuppressWarnings("unchecked")
        Map<StreamIdBasedExpression<Object>, StreamId<Object>> streamIds = leaves.stream()
                .filter(node -> node instanceof StreamIdBasedExpression)
                .map(node -> ((StreamIdBasedExpression<Object>) node))
                .collect(Collectors.toMap(exp -> exp, exp -> exp.streamId()));

        List<Observable<ExpToValue>> observableEntries = new ArrayList<>();

        for (Entry<StreamIdBasedExpression<Object>, StreamId<Object>> entry : streamIds.entrySet()) {
            Observable<?> plainObservable = rxFrom(discoveryService.discover(entry.getValue()));
            plainObservable.doOnNext((a) -> System.out.println(a));
            Observable<ExpToValue> mappedObservable = plainObservable.map(obj -> (new ExpToValue(entry.getKey(), obj)));
            observableEntries.add(mappedObservable);
        }

        return combineLatest(observableEntries, CONTEXT_COMBINER).map(ctx -> engine.resolveDetailed(expression, ctx));
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
