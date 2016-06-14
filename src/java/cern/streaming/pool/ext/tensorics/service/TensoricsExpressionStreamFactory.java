/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static cern.streaming.pool.core.util.ReactStreams.rxFrom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.resolve.engine.ResolvingEngines;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;
import org.tensorics.core.tree.domain.ResolvingContext;
import org.tensorics.core.tree.walking.Trees;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.util.ReactStreams;
import cern.streaming.pool.ext.tensorics.domain.ExpressionBasedStreamId;
import cern.streaming.pool.ext.tensorics.domain.StreamIdBasedExpression;
import rx.Observable;
import rx.functions.FuncN;

public class TensoricsExpressionStreamFactory implements StreamFactory {

    private static final FuncN<ResolvingContext> CONTEXT_COMBINER = new FuncN<ResolvingContext>() {

        @Override
        public ResolvingContext call(Object... entriesToCombine) {
            EditableResolvingContext context = Contexts.newResolvingContext();
            for (Object entry : entriesToCombine) {
                ExpToValue castedEntry = (ExpToValue) entry;
                context.put(castedEntry.node, castedEntry.value);
            }
            return context;
        }
    };

    private final ResolvingEngine engine = ResolvingEngines.defaultEngine();

    @Override
    public <T> ReactStream<T> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof ExpressionBasedStreamId)) {
            return null;
        }
        
        Expression<T> expression = ((ExpressionBasedStreamId<T>) id).expression();

        Collection<Node> leaves = Trees.findBottomNodes(expression);

        Map<StreamIdBasedExpression<Object>, StreamId<Object>> streamIds = leaves.stream()
                .filter(node -> node instanceof StreamIdBasedExpression)
                .map(node -> ((StreamIdBasedExpression<Object>) node))
                .collect(Collectors.toMap(exp -> exp, exp -> exp.streamId()));

        List<Observable<ExpToValue>> observableEntries = new ArrayList<>();
        
        for (Entry<StreamIdBasedExpression<Object>, StreamId<Object>> entry : streamIds.entrySet()) {
            Observable<?> plainObservable = rxFrom(discoveryService.discover(entry.getValue()));
            Observable<ExpToValue> mappedObservable = plainObservable.map(obj -> (new ExpToValue(entry.getKey(), obj)));
            observableEntries.add(mappedObservable);
        }

        Observable<T> observable = Observable.combineLatest(observableEntries, CONTEXT_COMBINER)
                .map(ctx -> engine.resolve(expression, ctx));
        return ReactStreams.fromRx(observable);
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
