/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tensorics.core.lang.DoubleScript;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactStream;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.util.ReactStreams;
import cern.streaming.pool.ext.tensorics.domain.ExpressionBasedStreamId;
import cern.streaming.pool.ext.tensorics.domain.StreamIdBasedExpression;
import rx.Observable;

/**
 * Unit tests for {@link TensoricsExpressionStreamFactory}
 * 
 * @author caguiler
 */
@RunWith(MockitoJUnitRunner.class)
public class TensoricsExpressionStreamFactoryTest {

    private static final StreamId<Double> ID_A = ReactStreams.namedId("a");
    private static final StreamId<Double> ID_B = ReactStreams.namedId("b");

    private static final Expression<Double> A = StreamIdBasedExpression.of(ID_A);
    private static final Expression<Double> B = StreamIdBasedExpression.of(ID_B);

    private static final Expression<Double> A_PLUS_B = mockExpression();

    @Mock
    private ExpressionBasedStreamId<Double> aPlusBstreamId;

    @Mock
    private DiscoveryService discoveryService;

    @Mock
    private TensoricsExpressionStreamFactory factoryUnderTest;

    @Before
    public void setUp() {
        factoryUnderTest = new TensoricsExpressionStreamFactory();
        when(aPlusBstreamId.getExpression()).thenReturn(A_PLUS_B);

        Observable<Double> first = Observable.interval(1, TimeUnit.SECONDS).map( i -> (i+1)*10D).limit(3);
        

        ReactStream<Double> firstReact = ReactStreams.fromRx(first);
        when(discoveryService.discover(ID_A)).thenReturn(firstReact);

        Observable<Double> second =Observable.interval(2, TimeUnit.SECONDS).map( i -> 2.0).limit(3);
        
        ReactStream<Double> secondReact = ReactStreams.fromRx(second);
        when(discoveryService.discover(ID_B)).thenReturn(secondReact);
    }

  

    @Test
    public void testCreateReturnsNullWhenANonExpressionBasedStreamIdIsProvided() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreate(){
        ReactStream<Double> resolvedExpression = factoryUnderTest.create(aPlusBstreamId, discoveryService);
   
        List<Double> values = ReactStreams.rxFrom(resolvedExpression).toList().toBlocking().single();
        
        values.stream().forEach(System.out::println);
        
        assertEquals(3, values.size());
    }

    private static Expression<Double> mockExpression() {

        return new DoubleScript<Double>() {

            @Override
            protected Expression<Double> describe() {
                return calculate(A).plus(B);
            }
        };

    }
    
    private void safeSleep(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}
