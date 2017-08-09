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

package org.streamingpool.ext.tensorics.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.reactivestreams.Publisher;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.testing.NamedStreamId;
import org.streamingpool.ext.tensorics.expression.StreamIdBasedExpression;
import org.streamingpool.ext.tensorics.streamfactory.DetailedTensoricsExpressionStreamFactory;
import org.streamingpool.ext.tensorics.streamfactory.TensoricsExpressionStreamFactory;
import org.streamingpool.ext.tensorics.streamid.DetailedExpressionStreamId;
import org.tensorics.core.lang.DoubleScript;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.resolve.engine.ResolvingEngines;
import org.tensorics.core.tree.domain.Expression;

import io.reactivex.Flowable;

/**
 * Unit tests for {@link TensoricsExpressionStreamFactory}
 * 
 * @author caguiler
 */
@RunWith(MockitoJUnitRunner.class)
public class TensoricsExpressionStreamFactoryTest {

    private static final StreamId<?> NO_TENSORICS_STREAM_ID = mock(StreamId.class);
    private static final StreamId<Double> ID_A = NamedStreamId.ofName("a");
    private static final StreamId<Double> ID_B = NamedStreamId.ofName("b");

    private static final Expression<Double> A = StreamIdBasedExpression.of(ID_A);
    private static final Expression<Double> B = StreamIdBasedExpression.of(ID_B);

    private static final Expression<Double> A_PLUS_B = mockExpression();

    private DetailedExpressionStreamId<Double, Expression<Double>> expressionBasedStreamId;

    @Mock
    private DiscoveryService discoveryService;

    private DetailedTensoricsExpressionStreamFactory factoryUnderTest;

    @Before
    public void setUp() {
        factoryUnderTest = new DetailedTensoricsExpressionStreamFactory(ResolvingEngines.defaultEngine());
        expressionBasedStreamId = DetailedExpressionStreamId.of(A_PLUS_B);

        mockStream1();
        mockStream2();
    }

    private void mockStream1() {
        Flowable<Double> first = Flowable.interval(100, TimeUnit.MILLISECONDS).map(i -> (i + 1) * 10D).take(3);
        when(discoveryService.discover(ID_A)).thenReturn(first);
    }

    private void mockStream2() {
        Flowable<Double> second = Flowable.interval(77, TimeUnit.MILLISECONDS).map(i -> 2.0).take(3);
        when(discoveryService.discover(ID_B)).thenReturn(second);
    }

    @Test
    public void testCreate() {
        Publisher<DetailedExpressionResult<Double, Expression<Double>>> resolvedExpression = factoryUnderTest
                .create(expressionBasedStreamId, discoveryService).data();

        List<Double> values = Flowable.fromPublisher(resolvedExpression).map(DetailedExpressionResult::value).toList()
                .blockingGet();

        assertEquals(5, values.size());
    }

    @Test
    public void testCanCreateWithCorrectStreamIdType() {
        assertTrue(factoryUnderTest.create(expressionBasedStreamId, discoveryService).isPresent());
    }

    @Test
    public void testCanCreateWithWrongStreamIdType() {
        assertFalse(factoryUnderTest.create(NO_TENSORICS_STREAM_ID, discoveryService).isPresent());
    }

    @Test
    public void testCanCreateWithNull() {
        assertFalse(factoryUnderTest.create(null, discoveryService).isPresent());
    }

    private static Expression<Double> mockExpression() {

        return new DoubleScript<Double>() {

            private static final long serialVersionUID = 1L;

            @Override
            protected Expression<Double> describe() {
                return calculate(A).plus(B);
            }
        };

    }

}
