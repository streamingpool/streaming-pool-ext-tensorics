/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

import java.util.Objects;

import cern.streaming.pool.core.service.StreamId;

/**
 * If this evaluation strategy is used, then the analysis is evaluated every time when a special trigger appears. This
 * strategy to use only the direct values of the used streams. For example, no explicit buffering can be performed for
 * the analysis.
 * 
 * @author kfuchsbe
 */
public class TriggeredEvaluation implements EvaluationStrategy {

    private final StreamId<?> triggeringStreamId;

    private TriggeredEvaluation(StreamId<?> triggeringStreamId) {
        this.triggeringStreamId = Objects.requireNonNull(triggeringStreamId, "triggeringStreamId must not be null");
    }

    /**
     * Factory method which requires one {@link StreamId} which can trigger a respective analysis.
     * 
     * @param triggeringStreamId the stream which will trigger the analysis
     * @throws NullPointerException if the the given id is {@code null}
     */
    public static TriggeredEvaluation triggeredBy(StreamId<?> triggeringStreamId) {
        return new TriggeredEvaluation(triggeringStreamId);
    }

    public StreamId<?> triggeringStreamId() {
        return triggeringStreamId;
    }

}
