/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.util.ReactStreams;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import rx.Observable;

/**
 * Creates non-overlapping buffers in the form of lists by means of a {@link BufferedStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
public class TensoricsBufferedStreamFactory implements StreamFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> ReactStream<T> create(StreamId<T> id, DiscoveryService discoveryService) {

        if (!(id instanceof BufferedStreamId)) {
            return null;
        }

        BufferedStreamId<T> bufferedStreamId = (BufferedStreamId<T>) id;
        
        Observable<List<T>> buffered = bufferedStream(discoveryService, bufferedStreamId);

        return (ReactStream<T>) ReactStreams.fromRx(buffered);
    }

    private <R> Observable<List<R>> bufferedStream(DiscoveryService discoveryService,
            BufferedStreamId<R> bufferedStreamId) {

        ReactStream<R> sourceStream = discoveryService.discover(bufferedStreamId.getSourceStream());

        Duration windowLength = bufferedStreamId.getWindowLength();

        return ReactStreams.rxFrom(sourceStream).buffer(windowLength.toNanos(), TimeUnit.NANOSECONDS);
    }

}
