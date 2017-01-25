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
import static org.junit.Assert.assertNotNull;
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
import org.reactivestreams.Publisher;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.testing.AbstractStreamTest;
import org.streamingpool.ext.tensorics.streamfactory.DiscreteFunctionStreamFactory;
import org.streamingpool.ext.tensorics.streamid.BufferedStreamId;
import org.streamingpool.ext.tensorics.streamid.FunctionStreamId;
import org.tensorics.core.commons.operations.Conversion;
import org.tensorics.core.function.DiscreteFunction;

import io.reactivex.Flowable;

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
    public void testCreate() {
        Publisher<DiscreteFunction<Integer, Double>> reactStream = factoryUnderTest
                .create(functionStreamId, discoveryService).get();

        assertNotNull(reactStream);

        Flowable<DiscreteFunction<Integer, Double>> rxFrom = Flowable.fromPublisher(reactStream);

        List<DiscreteFunction<Integer, Double>> functions = rxFrom.toList().blockingGet();

        assertEquals(2, functions.size());

        assertTrue(functions.stream().allMatch(f -> f.definedXValues().size() == 2));

    }

    @Test
    public void testCanCreateWithCorrectStreamIdType() {
        assertTrue(factoryUnderTest.create(functionStreamId, discoveryService).isPresent());
    }

    @Test
    public void testCanCreateWithWrongStreamIdType() {
        assertFalse(factoryUnderTest.create(bufferedStreamId, discoveryService).isPresent());
    }

    @Test
    public void testCanCreateWithNull() {
        assertFalse(factoryUnderTest.create(null, discoveryService).isPresent());
    }

    private void mockDiscoveryService() {
        List<Pair<Integer, Double>> list1 = Arrays.asList(Pair.of(1, 1.0), Pair.of(2, 2.0));
        List<Pair<Integer, Double>> list2 = Arrays.asList(Pair.of(3, 3.0), Pair.of(4, 4.0));

        Flowable<List<Pair<Integer, Double>>> source = Flowable.just(list1, list2);
        Publisher<List<Pair<Integer, Double>>> stream = Flowable.fromPublisher(source);
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
