/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamfactory;

import static cern.streaming.pool.core.service.util.ReactiveStreams.fromRx;
import static cern.streaming.pool.core.service.util.ReactiveStreams.rxFrom;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.util.Optional;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.streamid.BufferedStreamId;

/**
 * Creates non-overlapping buffers in the form of lists by means of a {@link BufferedStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
public class TensoricsBufferedStreamFactory implements StreamFactory {

    /* Safe, manually checked */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<ReactiveStream<T>> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof BufferedStreamId)) {
            return Optional.empty();
        }
        BufferedStreamId<T> bufferedId = (BufferedStreamId<T>) id;

        ReactiveStream<T> sourceStream = discoveryService.discover(bufferedId.getSourceStream());
        Duration windowLength = bufferedId.getWindowLength();

        return of((ReactiveStream<T>) fromRx(rxFrom(sourceStream).buffer(windowLength.toNanos(), NANOSECONDS)));
    }

}
