/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.streamingpool.ext.tensorics.streamfactory;

import static java.lang.String.format;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.streamingpool.core.domain.ErrorDeflector;
import org.streamingpool.core.domain.ErrorStreamPair;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamFactory;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.service.streamid.BufferSpecification;
import org.streamingpool.core.service.streamid.OverlapBufferStreamId;
import org.streamingpool.ext.tensorics.evaluation.BufferedEvaluation;
import org.streamingpool.ext.tensorics.evaluation.EvaluationStrategy;
import org.streamingpool.ext.tensorics.expression.BufferedStreamExpression;
import org.streamingpool.ext.tensorics.streamid.DetailedExpressionStreamId;
import org.tensorics.core.expressions.Placeholder;
import org.tensorics.core.resolve.engine.ResolvedContextDidNotGrowException;
import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.resolve.options.HandleWithFirstCapableAncestorStrategy;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.ResolvingContext;
import org.tensorics.core.tree.walking.EveryNodeCallback;
import org.tensorics.core.tree.walking.Trees;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

import io.reactivex.Flowable;

/**
 * {@link StreamFactory} that creates {@link DetailedExpressionStreamId}s with {@link BufferedEvaluation} as
 * {@link EvaluationStrategy}.
 * 
 * @author acalia
 * @see DetailedExpressionStreamId
 */
public class BufferedTensoricsExpressionStreamFactory implements StreamFactory {

    private static final HandleWithFirstCapableAncestorStrategy EXCEPTION_HANDLING_STRATEGY = new HandleWithFirstCapableAncestorStrategy();

    private final ResolvingEngine engine;

    public BufferedTensoricsExpressionStreamFactory(ResolvingEngine engine) {
        this.engine = engine;
    }

    @Override
    public <T> ErrorStreamPair<T> create(StreamId<T> id, DiscoveryService discovery) {
        if (!(id instanceof DetailedExpressionStreamId)) {
            return ErrorStreamPair.empty();
        }

        DetailedExpressionStreamId<?, ?> expressionStreamId = (DetailedExpressionStreamId<?, ?>) id;

        EvaluationStrategy evaluationStrategy = evaluationStrategy(expressionStreamId);

        if (!(evaluationStrategy instanceof BufferedEvaluation)) {
            return ErrorStreamPair.empty();
        }

        BufferSpecification bufferSpecification = ((BufferedEvaluation) evaluationStrategy).bufferSpecification();

        Expression<?> rootExpression = expressionStreamId.expression();
        ResolvingContext initialCtx = expressionStreamId.initialContext();

        List<BufferedStreamExpression<?>> bufferedStreamExpressions = allBufferedStreamExpressions(rootExpression);

        Multimap<StreamId<?>, BufferedStreamExpression<?>> streamIdToExpressions = streamIdToExpressions(
                bufferedStreamExpressions, initialCtx);
        Set<StreamId<?>> streamIds = streamIdToExpressions.keySet();

        Set<OverlapBufferStreamId<?>> bufferedStreamIds = bufferdStreamIds(streamIds, bufferSpecification);
        Set<Flowable<IdentifiedValue>> bufferedFlowables = discoverIdentifiedStreams(discovery, bufferedStreamIds);

        ErrorDeflector ed = ErrorDeflector.create();

        @SuppressWarnings({ "unchecked", "rawtypes" }) /* safe casts */
        Flowable<?> resultStream = Flowable.zip(bufferedFlowables, zipValues -> {
            EditableResolvingContext bufferedValuesCtx = Contexts.newResolvingContext();

            for (Object zipValue : zipValues) {
                IdentifiedValue identifiedValue = (IdentifiedValue) zipValue;
                OverlapBufferStreamId<?> streamId = (OverlapBufferStreamId<?>) identifiedValue.streamId;
                Object bufferValue = identifiedValue.value;

                Collection<BufferedStreamExpression<?>> expressionsUsingStreamId = streamIdToExpressions
                        .get(streamId.sourceId());
                expressionsUsingStreamId.forEach(expr -> bufferedValuesCtx.put((Expression) expr, bufferValue));
            }

            return bufferedValuesCtx;
        }).map(bufferedValuesCtx -> {
            EditableResolvingContext fullCtx = Contexts.newResolvingContext();
            @SuppressWarnings("cast") /* not building on java 1.8.0_131 without explicitly cast ... */
            ResolvingContext castedResolvingCtx = (ResolvingContext) bufferedValuesCtx;
            fullCtx.putAllNew(castedResolvingCtx);
            fullCtx.putAllNew(initialCtx);
            return fullCtx;
        }).map(ed.emptyOnException((ResolvingContext fullCtx) -> engine.resolveDetailed(rootExpression, fullCtx,
                EXCEPTION_HANDLING_STRATEGY))).share();

        @SuppressWarnings("unchecked") /* safe cast */
        Publisher<Optional<T>> castedResultStream = (Publisher<Optional<T>>) resultStream;
        return ed.streamNonEmpty(castedResultStream);
    }

    /**
     * Discovers each streamId and map each value to the wrapper
     * {@link BufferedTensoricsExpressionStreamFactory.IdentifiedValue} that holds the value and the streamId that
     * corresponds to the created {@link Flowable}.
     */
    private static Set<Flowable<IdentifiedValue>> discoverIdentifiedStreams(DiscoveryService discoveryService,
            Set<OverlapBufferStreamId<?>> bufferedStreamIds) {
        return bufferedStreamIds.stream().map(streamId -> {
            Flowable<?> stream = Flowable.fromPublisher(discoveryService.discover(streamId));
            return stream.map(value -> new IdentifiedValue(streamId, value));
        }).collect(Collectors.toSet());
    }

    private static Set<OverlapBufferStreamId<?>> bufferdStreamIds(Set<StreamId<?>> streamIds,
            BufferSpecification bufferSpecification) {
        return streamIds.stream().map(si -> OverlapBufferStreamId.of(si, bufferSpecification))
                .collect(Collectors.toSet());
    }

    /**
     * A {@link BufferedStreamExpression} has an {@link Expression} of a {@link StreamId}. This method resolves the
     * {@link Expression}. Furthermore, it keeps reference of which {@link BufferedStreamExpression} contains a given
     * {@link StreamId}. Since there may be different {@link BufferedStreamExpression} containing the same
     * {@link StreamId}, the result is a {@link Multimap}.
     */
    @VisibleForTesting
    Multimap<StreamId<?>, BufferedStreamExpression<?>> streamIdToExpressions(
            List<BufferedStreamExpression<?>> bufferedStreamExpressions, ResolvingContext ctx) {
        Builder<StreamId<?>, BufferedStreamExpression<?>> builder = ImmutableMultimap.builder();

        for (BufferedStreamExpression<?> bufferedStreamExpression : bufferedStreamExpressions) {
            StreamId<?> streamId = resolveStreamIdExpression(bufferedStreamExpression, ctx);
            builder.put(streamId, bufferedStreamExpression);
        }

        return builder.build();
    }

    private StreamId<?> resolveStreamIdExpression(BufferedStreamExpression<?> bufferedExpr, ResolvingContext ctx) {
        Expression<?> streamIdExpression = bufferedExpr.streamIdExpression();
        try {
            return (StreamId<?>) engine.resolve(streamIdExpression, ctx);
        } catch (ResolvedContextDidNotGrowException exc) {
            throw new RuntimeException(format("Cannot resolve streamid expression %s of buffered expression %s",
                    streamIdExpression, bufferedExpr), exc);
        }
    }

    /**
     * Walks the tree of the given {@link Expression} and returns all the {@link BufferedStreamExpression} nodes.
     */
    @VisibleForTesting
    static List<BufferedStreamExpression<?>> allBufferedStreamExpressions(Expression<?> rootExpression) {
        List<BufferedStreamExpression<?>> bufferedExpressions = new LinkedList<>();
        EveryNodeCallback callback = node -> {
            if (node instanceof BufferedStreamExpression) {
                bufferedExpressions.add((BufferedStreamExpression<?>) node);
            }
        };
        /* Using the generic walk because there may be duplicates and we want them */
        Trees.walkParentAfterChildren(rootExpression, callback);
        if (bufferedExpressions.isEmpty()) {
            throw new IllegalArgumentException(
                    "The specified root expression does not contain any BufferedStreamExpression: " + rootExpression);
        }
        return bufferedExpressions;
    }

    private static EvaluationStrategy evaluationStrategy(DetailedExpressionStreamId<?, ?> expressionStreamId) {
        Placeholder<EvaluationStrategy> placeholder = Placeholder.ofClass(EvaluationStrategy.class);
        if (!expressionStreamId.initialContext().resolves(placeholder)) {
            throw new IllegalStateException(
                    "Initial context must provide a value for the placeholder of an EvaluationStrategy");
        }
        return expressionStreamId.initialContext().resolvedValueOf(placeholder);
    }

    /**
     * Simple object that wrap a value with the {@link StreamId} that generated the {@link Publisher} that published the
     * value
     */
    private static class IdentifiedValue {
        private final StreamId<?> streamId;
        private final Object value;

        public IdentifiedValue(StreamId<?> streamId, Object value) {
            this.streamId = streamId;
            this.value = value;
        }

        @Override
        public String toString() {
            return "IdentifiedValue [streamId=" + streamId + ", value=" + value + "]";
        }

    }

}
