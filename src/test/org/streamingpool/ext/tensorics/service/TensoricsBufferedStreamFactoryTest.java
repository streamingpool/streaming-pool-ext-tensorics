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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.reactivestreams.Publisher;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.testing.NamedStreamId;
import org.streamingpool.ext.tensorics.streamfactory.TensoricsBufferedStreamFactory;
import org.streamingpool.ext.tensorics.streamid.BufferedStreamId;

import io.reactivex.Flowable;

/**
 * Unit tests for {@link TensoricsBufferedStreamFactory}
 * 
 * @author caguiler
 */
@RunWith(MockitoJUnitRunner.class)
public class TensoricsBufferedStreamFactoryTest {

    @Mock
    private DiscoveryService discoveryService;

    @Mock
    private BufferedStreamId<Integer> bufferedStreamId;

    @Mock
    private NamedStreamId<Integer> invalidStreamId;

    @Mock
    private StreamId<Integer> streamId;

    private Duration windowsLength;

    private TensoricsBufferedStreamFactory factoryUnderTest;

    @Before
    public void setUp() {
        factoryUnderTest = new TensoricsBufferedStreamFactory();

        mockBufferedStreamId();
        mockDiscoveryService();
    }

    @Test
    public void testCreate() {
        Publisher<List<Integer>> createdStream = factoryUnderTest.create(bufferedStreamId, discoveryService).data();

        verify(bufferedStreamId).getSourceStream();
        verify(bufferedStreamId).getWindowLength();
        verify(discoveryService).discover(streamId);
        assertNotNull(createdStream);
    }

    @Test
    public void testCanCreateWithCorrectStreamIdType() {
        assertTrue(factoryUnderTest.create(bufferedStreamId, discoveryService).isPresent());
    }

    @Test
    public void testCanCreateWithWrongStreamIdType() {
        assertFalse(factoryUnderTest.create(invalidStreamId, discoveryService).isPresent());
    }

    @Test
    public void testCanCreateWithNull() {
        assertFalse(factoryUnderTest.create(null, discoveryService).isPresent());
    }

    private void mockDiscoveryService() {
        Publisher<Integer> stream = Flowable.just(1);
        when(discoveryService.discover(streamId)).thenReturn(stream);
    }

    private void mockBufferedStreamId() {
        when(bufferedStreamId.getSourceStream()).thenReturn(streamId);
        windowsLength = Duration.ofMillis(10);
        when(bufferedStreamId.getWindowLength()).thenReturn(windowsLength);
    }
}
