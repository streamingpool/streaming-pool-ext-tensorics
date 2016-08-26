/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tensorics.core.lang.DoubleScript;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.resolve.engine.ResolvingEngines;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.util.ReactiveStreams;
import cern.streaming.pool.core.testing.NamedStreamId;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import cern.streaming.pool.ext.tensorics.domain.DetailedExpressionStreamId;
import cern.streaming.pool.ext.tensorics.domain.StreamIdBasedExpression;
import rx.Observable;

/**
 * Unit tests for {@link TensoricsExpressionStreamFactory}
 * 
 * @author caguiler
 */
@RunWith(MockitoJUnitRunner.class)
public class TensoricsExpressionStreamFactoryTest {

    private static final StreamId<Double> ID_A = NamedStreamId.ofName("a");
    private static final StreamId<Double> ID_B = NamedStreamId.ofName("b");

    private static final Expression<Double> A = StreamIdBasedExpression.of(ID_A);
    private static final Expression<Double> B = StreamIdBasedExpression.of(ID_B);

    private static final Expression<Double> A_PLUS_B = mockExpression();

    @Mock
    private DetailedExpressionStreamId<Double, Expression<Double>> expressionBasedStreamId;

    @Mock
    private BufferedStreamId<Integer> invalidStreamId;
    
    @Mock
    private DiscoveryService discoveryService;

    private DetailedTensoricsExpressionStreamFactory<Double, Expression<Double>> factoryUnderTest;

    @Before
    public void setUp() {
        factoryUnderTest = new DetailedTensoricsExpressionStreamFactory<>(ResolvingEngines.defaultEngine());
        when(expressionBasedStreamId.getExpression()).thenReturn(A_PLUS_B);

        mockStream1();
        mockStream2();
    }

    private void mockStream1() {
        Observable<Double> first = Observable.interval(100, TimeUnit.MILLISECONDS).map(i -> (i + 1) * 10D).limit(3);
        ReactiveStream<Double> firstReact = ReactiveStreams.fromRx(first);
        when(discoveryService.discover(ID_A)).thenReturn(firstReact);
    }

    private void mockStream2() {
        Observable<Double> second = Observable.interval(77, TimeUnit.MILLISECONDS).map(i -> 2.0).limit(3);
        ReactiveStream<Double> secondReact = ReactiveStreams.fromRx(second);
        when(discoveryService.discover(ID_B)).thenReturn(secondReact);
    }

    @Test
    public void testCreate() {
        ReactiveStream<DetailedExpressionResult<Double, Expression<Double>>> resolvedExpression = factoryUnderTest
                .create(expressionBasedStreamId, discoveryService);

        List<Double> values = ReactiveStreams.rxFrom(resolvedExpression).map(DetailedExpressionResult::value).toList()
                .toBlocking().single();

        assertEquals(5, values.size());
    }

    @Test
    public void testCanCreateWithCorrectStreamIdType() {
        assertTrue(factoryUnderTest.canCreate(expressionBasedStreamId));
    }

    @Test
    public void testCanCreateWithWrongStreamIdType() {
        assertFalse(factoryUnderTest.canCreate(invalidStreamId));
    }
    
    @Test
    public void testCanCreateWithNull() {
        assertFalse(factoryUnderTest.canCreate(null));
    }
    
    private static Expression<Double> mockExpression() {

        return new DoubleScript<Double>() {

            @Override
            protected Expression<Double> describe() {
                return calculate(A).plus(B);
            }
        };

    }

}
