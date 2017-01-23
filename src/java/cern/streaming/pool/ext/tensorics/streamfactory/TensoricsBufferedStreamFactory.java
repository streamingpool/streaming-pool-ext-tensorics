/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamfactory;

import static io.reactivex.Flowable.fromPublisher;
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.util.Optional;

import org.reactivestreams.Publisher;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.streamid.BufferedStreamId;

/**
 * Creates non-overlapping buffers in the form of lists by means of a {@link BufferedStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
@Deprecated
public class TensoricsBufferedStreamFactory implements StreamFactory {

    /* Safe, manually checked */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<Publisher<T>> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof BufferedStreamId)) {
            return Optional.empty();
        }
        BufferedStreamId<T> bufferedId = (BufferedStreamId<T>) id;

        Publisher<T> sourceStream = discoveryService.discover(bufferedId.getSourceStream());
        Duration windowLength = bufferedId.getWindowLength();

        return of((Publisher<T>) fromPublisher(sourceStream).buffer(windowLength.toNanos(), NANOSECONDS));
    }

}
