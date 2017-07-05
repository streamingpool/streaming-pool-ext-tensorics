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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.tensorics.core.resolve.engine.ResolvingEngines.defaultEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.ext.tensorics.expression.StreamIdBasedExpression;
import org.streamingpool.ext.tensorics.expression.UnresolvedStreamIdBasedExpression;
import org.streamingpool.ext.tensorics.streamid.DetailedExpressionStreamId;
import org.tensorics.core.tree.domain.AbstractDeferredExpression;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;
import org.tensorics.core.tree.domain.ResolvingContext;
import org.tensorics.core.tree.domain.ResolvingContextImpl;

public class DetailedTensoricsExpressionStreamFactoryUnresolvedStreamIdExpressionTest {

    private static final DetailedTensoricsExpressionStreamFactory DETAILED_TENSORICS_EXPRESSION_STREAM_FACTORY = new DetailedTensoricsExpressionStreamFactory(
            defaultEngine());

    @Test
    public void testWithStreamIdBasedExpression() {
        StreamId<Object> streamIdA = mockStreamId();
        StreamIdBasedExpression<Object> resolvedExpressionA = StreamIdBasedExpression.of(streamIdA);

        assertThat(extractStreamIds(resolvedExpressionA)).containsValue(streamIdA);
    }

    @Test
    public void testWithUnresolvedStreamIdBasedExpression() {
        StreamId<Object> streamIdA = mockStreamId();
        Expression<StreamId<Object>> streamExpressionA = mockStreamIdExpression();

        EditableResolvingContext initialCtx = Contexts.newResolvingContext();
        initialCtx.put(streamExpressionA, streamIdA);

        UnresolvedStreamIdBasedExpression<Object> unresolvedExpressionA = new UnresolvedStreamIdBasedExpression<>(
                streamExpressionA);

        assertThat(extractStreamIds(initialCtx, unresolvedExpressionA)).containsValue(streamIdA);
    }

    @Test
    public void testWithBothStreamIdBasedExpressions() {
        StreamId<Object> streamIdA = mockStreamId();
        StreamIdBasedExpression<Object> resolvedExpressionA = StreamIdBasedExpression.of(streamIdA);

        StreamId<Object> streamIdB = mockStreamId();
        Expression<StreamId<Object>> streamExpressionB = mockStreamIdExpression();

        EditableResolvingContext initialCtx = Contexts.newResolvingContext();
        initialCtx.put(streamExpressionB, streamIdB);

        UnresolvedStreamIdBasedExpression<Object> unresolvedExpressionB = new UnresolvedStreamIdBasedExpression<>(
                streamExpressionB);

        assertThat(extractStreamIds(initialCtx, resolvedExpressionA, unresolvedExpressionB)).containsValue(streamIdA)
                .containsValue(streamIdB);

    }

    private Map<Expression<Object>, StreamId<Object>> extractStreamIds(Expression<?>... expressions) {
        return extractStreamIds(new ResolvingContextImpl(), expressions);
    }

    private Map<Expression<Object>, StreamId<Object>> extractStreamIds(ResolvingContext initialCtx,
            Expression<?>... expressions) {
        GroupExpression group = new GroupExpression(Arrays.asList(expressions));
        return DETAILED_TENSORICS_EXPRESSION_STREAM_FACTORY
                .streamIdsFrom(mockDetailedExpressionStreamId(initialCtx, group));
    }

    @SuppressWarnings("unchecked")
    private DetailedExpressionStreamId<Void, GroupExpression> mockDetailedExpressionStreamId(
            ResolvingContext initialCtx, GroupExpression group) {
        DetailedExpressionStreamId<Void, GroupExpression> detailedId = mock(DetailedExpressionStreamId.class);
        when(detailedId.expression()).thenReturn(group);
        when(detailedId.initialCtx()).thenReturn(initialCtx);
        return detailedId;
    }

    @SuppressWarnings("unchecked")
    private StreamId<Object> mockStreamId() {
        return mock(StreamId.class);
    }

    @SuppressWarnings("unchecked")
    private Expression<StreamId<Object>> mockStreamIdExpression() {
        return mock(Expression.class);
    }

    /**
     * Simple group expression that declares the List parameter as children. It cannot be resolved as it is not the
     * purpose of the test.
     */
    private static class GroupExpression extends AbstractDeferredExpression<Void> {

        private final List<Expression<?>> expressions;

        public GroupExpression(List<Expression<?>> expressions) {
            super();
            this.expressions = expressions;
        }

        @Override
        public List<? extends Node> getChildren() {
            return expressions;
        }

    }
}
