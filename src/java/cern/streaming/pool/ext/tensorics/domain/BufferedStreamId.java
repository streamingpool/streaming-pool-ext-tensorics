/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;

import org.tensorics.core.function.DiscreteFunction;

import cern.streaming.pool.core.service.StreamId;

public class BufferedStreamId<R> implements StreamId<DiscreteFunction<Instant, R>> {

    /**
     * @param sourceStream
     * @param windowLength
     */
    public BufferedStreamId(StreamId<R> sourceStream, Duration windowLength) {
        super();
        this.sourceStream = sourceStream;
        this.windowLength = windowLength;
    }

    private final StreamId<R> sourceStream;
    private final Duration windowLength;

}
