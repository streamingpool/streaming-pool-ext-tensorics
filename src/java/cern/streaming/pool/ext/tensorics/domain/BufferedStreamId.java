/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.domain;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import cern.streaming.pool.core.service.StreamId;

/**
 * Stream id useful to look up a stream of buffered values of <code>R</code>. The buffer length can be tuned by a
 * Duration.
 * <p>
 * If no buffer length is given it is set to 1 second by default.
 * 
 * @author caguiler
 * @param <R> type of the values to buffering
 */
public class BufferedStreamId<R> implements StreamId<List<R>> {

    private final StreamId<R> sourceStream;
    private final Duration windowLength;

    /**
     * Builds a {@link BufferedStreamId}
     * 
     * @param sourceStream stream id to buffer
     */
    public BufferedStreamId(StreamId<R> sourceStream) {
        this(sourceStream, Duration.ofSeconds(1));
    }

    /**
     * Builds a {@link BufferedStreamId}
     * 
     * @param sourceStream stream id to buffer
     * @param windowLength a duration representing the buffer window length
     */
    public BufferedStreamId(StreamId<R> sourceStream, Duration windowLength) {
        super();
        Objects.requireNonNull(sourceStream, "sourceStream cannot be null");
        Objects.requireNonNull(windowLength, "windowLength canont be null");
        this.sourceStream = sourceStream;
        this.windowLength = windowLength;
    }

    public StreamId<R> getSourceStream() {
        return sourceStream;
    }

    public Duration getWindowLength() {
        return windowLength;
    }
}
