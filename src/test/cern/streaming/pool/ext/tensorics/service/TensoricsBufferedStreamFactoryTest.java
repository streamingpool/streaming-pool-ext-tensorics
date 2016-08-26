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

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.util.ReactiveStreams;
import cern.streaming.pool.core.testing.NamedStreamId;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import rx.Observable;

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

    private TensoricsBufferedStreamFactory<Integer> factoryUnderTest;

    @Before
    public void setUp() {
        factoryUnderTest = new TensoricsBufferedStreamFactory<>();

        mockBufferedStreamId();
        mockDiscoveryService();
    }

    @Test
    public void testCreate() {
        ReactiveStream<List<Integer>> createdStream = factoryUnderTest.create(bufferedStreamId, discoveryService);

        verify(bufferedStreamId).getSourceStream();
        verify(bufferedStreamId).getWindowLength();
        verify(discoveryService).discover(streamId);
        assertNotNull(createdStream);
    }

    @Test
    public void testCanCreateWithCorrectStreamIdType() {
        assertTrue(factoryUnderTest.canCreate(bufferedStreamId));
    }

    @Test
    public void testCanCreateWithWrongStreamIdType() {
        assertFalse(factoryUnderTest.canCreate(invalidStreamId));
    }
    
    @Test
    public void testCanCreateWithNull() {
        assertFalse(factoryUnderTest.canCreate(null));
    }
    
    private void mockDiscoveryService() {
        ReactiveStream<Integer> stream = ReactiveStreams.fromRx(Observable.just(1));
        when(discoveryService.discover(streamId)).thenReturn(stream);
    }

    private void mockBufferedStreamId() {
        when(bufferedStreamId.getSourceStream()).thenReturn(streamId);
        windowsLength = Duration.ofMillis(10);
        when(bufferedStreamId.getWindowLength()).thenReturn(windowsLength);
    }
}
