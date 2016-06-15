/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.domain;

import java.time.Duration;

import cern.streaming.pool.core.service.StreamId;

public class BufferedStreamId<R> implements StreamId<Iterable<R>> {

    private final StreamId<R> sourceStream;
    private final Duration windowLength;
    

    public BufferedStreamId(StreamId<R> sourceStream, Duration windowLength) {
        super();
        this.sourceStream = sourceStream;
        this.windowLength = windowLength;
    }

    public StreamId<R> sourceStream() {
        return sourceStream;
    }

    public Duration windowLength() {
        return windowLength;
    }
}
