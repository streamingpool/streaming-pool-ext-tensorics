/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.tensorics.core.commons.operations.Conversion;
import org.tensorics.core.function.DiscreteFunction;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.util.ReactiveStreams;
import cern.streaming.pool.core.testing.AbstractStreamTest;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import cern.streaming.pool.ext.tensorics.domain.FunctionStreamId;
import rx.Observable;

/**
 * Unit test sfor {@link DiscreteFunctionStreamFactory}
 * 
 * @author caguiler
 */
@RunWith(MockitoJUnitRunner.class)
public class DiscreteFunctionStreamFactoryTest extends AbstractStreamTest {

    @Mock
    private DiscoveryService discoveryService;

    @Mock
    private BufferedStreamId<Pair<Integer, Double>> bufferedStreamId;

    @Mock
    private FunctionStreamId<Pair<Integer, Double>, Integer, Double> functionStreamId;

    private DiscreteFunctionStreamFactory factoryUnderTest;

    @Before
    public void setUp() {
        factoryUnderTest = new DiscreteFunctionStreamFactory();
        mockFunctionStreamId();
        mockDiscoveryService();

    }

    @Test
    public void testCreateReturnsNullWhenANonFunctionStreamIdIsProvided() {
        ReactiveStream<?> reactStream = factoryUnderTest.create(bufferedStreamId, discoveryService);
        assertNull(reactStream);
    }

    @Test
    public void testCreate() {
        ReactiveStream<DiscreteFunction<Integer, Double>> reactStream = factoryUnderTest.create(functionStreamId,
                discoveryService);

        assertNotNull(reactStream);

        Observable<DiscreteFunction<Integer, Double>> rxFrom = ReactiveStreams.rxFrom(reactStream);

        List<DiscreteFunction<Integer, Double>> functions = rxFrom.toList().toBlocking().single();

        assertEquals(2, functions.size());

        assertTrue(functions.stream().allMatch(f -> f.definedXValues().size() == 2));

    }

    private void mockDiscoveryService() {
        List<Pair<Integer, Double>> list1 = Arrays.asList(Pair.of(1, 1.0), Pair.of(2, 2.0));
        List<Pair<Integer, Double>> list2 = Arrays.asList(Pair.of(3, 3.0), Pair.of(4, 4.0));

        Observable<List<Pair<Integer, Double>>> source = Observable.just(list1, list2);
        ReactiveStream<List<Pair<Integer, Double>>> stream = ReactiveStreams.fromRx(source);
        when(discoveryService.discover(bufferedStreamId)).thenReturn(stream);

    }

    private void mockFunctionStreamId() {
        when(functionStreamId.getSourceStream()).thenReturn(bufferedStreamId);
        Conversion<Pair<Integer, Double>, Integer> toX = Pair::getLeft;
        Conversion<Pair<Integer, Double>, Double> toY = Pair::getRight;
        doReturn(toX).when(functionStreamId).getToX();
        doReturn(toY).when(functionStreamId).getToY();
    }

}
