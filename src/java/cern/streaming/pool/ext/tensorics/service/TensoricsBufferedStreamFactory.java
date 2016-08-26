/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.util.ReactiveStreams;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import rx.Observable;

/**
 * Creates non-overlapping buffers in the form of lists by means of a {@link BufferedStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
public class TensoricsBufferedStreamFactory<R> implements StreamFactory<List<R>, BufferedStreamId<R>> {

    @Override
    public ReactiveStream<List<R>> create(BufferedStreamId<R> id, DiscoveryService discoveryService) {
        return ReactiveStreams.fromRx(bufferedStream(discoveryService, id));
    }

    @Override
    public boolean canCreate(StreamId<?> id) {
        return id instanceof BufferedStreamId;
    }

    private Observable<List<R>> bufferedStream(DiscoveryService discoveryService,
            BufferedStreamId<R> bufferedStreamId) {

        ReactiveStream<R> sourceStream = discoveryService.discover(bufferedStreamId.getSourceStream());

        Duration windowLength = bufferedStreamId.getWindowLength();

        /// XXX: Sliding window ??
        return ReactiveStreams.rxFrom(sourceStream).buffer(windowLength.toNanos(), TimeUnit.NANOSECONDS);
    }

}
