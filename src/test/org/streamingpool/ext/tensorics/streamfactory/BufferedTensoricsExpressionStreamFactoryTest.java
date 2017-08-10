/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.streamingpool.ext.tensorics.streamfactory;

import static com.google.common.collect.ImmutableList.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.streamingpool.ext.tensorics.expression.BufferedStreamExpression.buffer;
import static org.tensorics.core.lang.TensoricExpressions.use;
import static org.tensorics.core.tree.domain.Contexts.newResolvingContext;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.ext.tensorics.evaluation.BufferedEvaluation;
import org.streamingpool.ext.tensorics.evaluation.EvaluationStrategy;
import org.streamingpool.ext.tensorics.expression.BufferedStreamExpression;
import org.streamingpool.ext.tensorics.streamid.DetailedExpressionStreamId;
import org.tensorics.core.expressions.Placeholder;
import org.tensorics.core.resolve.engine.ResolvingEngines;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.ResolvedExpression;

import com.google.common.collect.Multimap;

public class BufferedTensoricsExpressionStreamFactoryTest {

    private BufferedTensoricsExpressionStreamFactory factory;

    @Before
    public void setUp() {
        factory = new BufferedTensoricsExpressionStreamFactory(ResolvingEngines.defaultEngine());
    }

    @Test
    public void testAllBufferedStreamExpressions() {
        BufferedStreamExpression<Object> bufferExpr1 = mockBufferedStreamExpression();
        BufferedStreamExpression<Object> bufferExpr2 = mockBufferedStreamExpression();
        Expression<?> root = use(bufferExpr1, bufferExpr2).in((b1, b2) -> true);

        assertThat(BufferedTensoricsExpressionStreamFactory.allBufferedStreamExpressions(root))
                .containsOnly(bufferExpr1, bufferExpr2);
    }

    @Test
    public void testStreamIdToExpressions() {
        StreamId<Integer> streamId1 = mockStreamId();
        StreamId<Integer> streamId2 = mockStreamId();

        Expression<StreamId<Integer>> streamIdExpression1 = mockExpression();
        Expression<StreamId<Integer>> streamIdExpression2 = mockExpression();

        BufferedStreamExpression<Integer> bufferOfStreamId1 = buffer(streamIdExpression1);
        BufferedStreamExpression<Integer> bufferOfStreamId2 = buffer(streamIdExpression2);

        EditableResolvingContext ctx = Contexts.newResolvingContext();
        ctx.put(streamIdExpression1, streamId1);
        ctx.put(streamIdExpression2, streamId2);

        Multimap<StreamId<?>, BufferedStreamExpression<?>> streamIdToExpressions = factory
                .streamIdToExpressions(of(bufferOfStreamId1, bufferOfStreamId2), ctx);

        assertThat(streamIdToExpressions.keySet()).containsOnly(streamId1, streamId2);
        assertThat(streamIdToExpressions.get(streamId1)).containsOnly(bufferOfStreamId1);
        assertThat(streamIdToExpressions.get(streamId2)).containsOnly(bufferOfStreamId2);
    }

    @Test(expected = IllegalStateException.class)
    public void testSourceStreamIdIsNotInInitialContext() {
        Expression<StreamId<Integer>> unresolvedStreamIdExpression = mockExpression();
        factory.streamIdToExpressions(
                Collections.singletonList(BufferedStreamExpression.buffer(unresolvedStreamIdExpression)),
                Contexts.newResolvingContext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoBufferedExpressions() {
        BufferedEvaluation evaluationStrategy = mock(BufferedEvaluation.class);
        factory.create(
                DetailedExpressionStreamId.of(ResolvedExpression.of("any"), newResolvingContext(), evaluationStrategy),
                mock(DiscoveryService.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlaceholderForEvaluationStrategyThrows() {
        EditableResolvingContext ctx = Contexts.newResolvingContext();
        ctx.put(Placeholder.ofClass(EvaluationStrategy.class), mock(EvaluationStrategy.class));
        DetailedExpressionStreamId<?, ?> streamId = DetailedExpressionStreamId.of(mockExpression(), ctx,
                mock(EvaluationStrategy.class));
        factory.create(streamId, mock(DiscoveryService.class));
    }

    @SuppressWarnings("unchecked")
    private static <T> StreamId<T> mockStreamId() {
        return mock(StreamId.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> Expression<T> mockExpression() {
        return mock(Expression.class);
    }

    @SuppressWarnings("unchecked")
    private static <T> BufferedStreamExpression<T> mockBufferedStreamExpression() {
        return mock(BufferedStreamExpression.class);
    }

}
