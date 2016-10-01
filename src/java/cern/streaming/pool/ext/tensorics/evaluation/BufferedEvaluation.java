/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

import static java.util.Objects.requireNonNull;

import java.time.Duration;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.streamid.BufferSpecification;

/**
 * This evaluation strategy allows buffering of streams. To be able to do so, it needs a stream which starts the buffers
 * and one which stops them.
 * 
 * @author kfuchsbe
 */
public class BufferedEvaluation implements EvaluationStrategy {

    private final BufferSpecification bufferSpecification;

    private BufferedEvaluation(BufferSpecification bufferSpecification) {
        this.bufferSpecification = requireNonNull(bufferSpecification, "bufferSpecification must not be null");
    }

    /**
     * Factory method which requires one stream id to start the buffering and one to stop it. The stopId will also
     * trigger the evaluation.
     * 
     * @param startStreamId the id of the stream that starts the buffering
     * @param endStreamId the id of the stream that ends the buffering
     * @throws NullPointerException if one of the given ids is {@code null}
     */
    public static final BufferedEvaluation ofStartAndEnd(StreamId<?> startStreamId, StreamId<?> endStreamId) {
        return new BufferedEvaluation(BufferSpecification.ofStartAndEnd(startStreamId, endStreamId));
    }

    public static final BufferedEvaluation ofStartAndEndTimeout(StreamId<?> startStreamId, StreamId<?> endStreamId,
            Duration timeout) {
        return new BufferedEvaluation(BufferSpecification.ofStartEndTimeout(startStreamId, endStreamId, timeout));
    }

    public BufferSpecification bufferSpecification() {
        return bufferSpecification;
    }

}
