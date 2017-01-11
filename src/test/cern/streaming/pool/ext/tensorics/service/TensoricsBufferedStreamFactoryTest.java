/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

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

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.testing.NamedStreamId;
import cern.streaming.pool.ext.tensorics.streamfactory.TensoricsBufferedStreamFactory;
import cern.streaming.pool.ext.tensorics.streamid.BufferedStreamId;
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
        Publisher<List<Integer>> createdStream = factoryUnderTest.create(bufferedStreamId, discoveryService).get();

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
