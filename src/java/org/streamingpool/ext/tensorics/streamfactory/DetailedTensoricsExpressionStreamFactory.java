// @formatter:off
/**
*
* This file is part of streaming pool (http://www.streamingpool.org).
*
* Copyright (c) 2017-present, CERN. All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
// @formatter:on

package org.streamingpool.ext.tensorics.streamfactory;

import static io.reactivex.Flowable.fromPublisher;
import static io.reactivex.Flowable.zip;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamingpool.core.domain.ErrorStreamPair;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamFactory;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.service.streamid.OverlapBufferStreamId;
import org.streamingpool.ext.tensorics.evaluation.BufferedEvaluation;
import org.streamingpool.ext.tensorics.evaluation.ContinuousEvaluation;
import org.streamingpool.ext.tensorics.evaluation.EvaluationStrategy;
import org.streamingpool.ext.tensorics.evaluation.TriggeredEvaluation;
import org.streamingpool.ext.tensorics.exception.NoBufferedStreamSpecifiedException;
import org.streamingpool.ext.tensorics.expression.UnresolvedStreamIdBasedExpression;
import org.streamingpool.ext.tensorics.streamid.DetailedExpressionStreamId;
import org.tensorics.core.expressions.Placeholder;
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
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

/**
 * {@link StreamFactory} that creates {@link DetailedExpressionStreamId}s with {@link ContinuousEvaluation} and
 * {@link TriggeredEvaluation} as {@link EvaluationStrategy}.
 *
 * @author acalia
 */
public class DetailedTensoricsExpressionStreamFactory implements StreamFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetailedTensoricsExpressionStreamFactory.class);

    private static final HandleWithFirstCapableAncestorStrategy EXCEPTION_HANDLING_STRATEGY = new HandleWithFirstCapableAncestorStrategy();

    private static final Function<Object[], Boolean> TRIGGER_CONTEXT_COMBINER = (Object... entriesToCombine) -> true;

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
    public <T> ErrorStreamPair<T> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof DetailedExpressionStreamId)) {
            return ErrorStreamPair.empty();
        }
        DetailedExpressionStreamId<?, ?> tensoricsId = (DetailedExpressionStreamId<?, ?>) id;

        /* NOTE!!!!!! This class does not creates buffered ids anymore, use BufferedTensoricsExpressionStreamFactory */
        if (tensoricsId.initialContext()
                .resolvedValueOf(Placeholder.ofClass(EvaluationStrategy.class)) instanceof BufferedEvaluation) {
            return ErrorStreamPair.empty();
        }

        return ErrorStreamPair.ofData((Flowable<T>) resolvedStream(tensoricsId, discoveryService));
    }

    private <T, E extends Expression<T>> Flowable<DetailedExpressionResult<T, E>> resolvedStream(
            DetailedExpressionStreamId<T, E> id, DiscoveryService discoveryService) {
        E expression = id.expression();
        ResolvingContext initialCtx = id.initialContext();

        Map<Expression<Object>, StreamId<Object>> streamIds = streamIdsFrom(id);
        ImmutableMultimap.Builder<StreamId<?>, Flowable<ExpToValue>> builder = ImmutableMultimap.builder();
        for (Entry<Expression<Object>, StreamId<Object>> entry : streamIds.entrySet()) {
            Flowable<?> plainObservable = fromPublisher(discoveryService.discover(entry.getValue()));
            Flowable<ExpToValue> mappedObservable;
            mappedObservable = plainObservable.map(obj -> new ExpToValue(entry.getKey(), obj));
            builder.put(entry.getValue(), mappedObservable);
        }
        ImmutableMultimap<StreamId<?>, Flowable<ExpToValue>> observableEntries = builder.build();

        EvaluationStrategy evaluationStrategy = initialCtx
                .resolvedValueOf(Placeholder.ofClass(EvaluationStrategy.class));

        Flowable<?> triggerObservable = triggerObservable(observableEntries, evaluationStrategy, discoveryService);
        return triggerObservable.withLatestFrom(observableEntries.values().toArray(new Flowable[] {}), CONTEXT_COMBINER)
                .map(ctx -> {
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
        ResolvingContext initialCtx = id.initialContext();

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

    private static Flowable<?> triggerObservable(Multimap<StreamId<?>, ? extends Flowable<?>> flowables,
            EvaluationStrategy strategy, DiscoveryService discoveryService) {
        if (strategy instanceof ContinuousEvaluation) {
            Collection<? extends Flowable<?>> streams = flowables.values();
            if (streams.isEmpty()) {
                LOGGER.warn("The expression does not contain any streams. "
                        + "Therefore it will never emit! This rarely might be what you want ;-)");
            }
            return Flowable.combineLatest(streams, TRIGGER_CONTEXT_COMBINER);
        }
        if (strategy instanceof BufferedEvaluation) {
            List<? extends Flowable<?>> triggeringObservables = flowables.entries().stream()
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

        @Override
        public String toString() {
            return "ExpToValue [node=" + node + ", value=" + value + "]";
        }

    }

}
