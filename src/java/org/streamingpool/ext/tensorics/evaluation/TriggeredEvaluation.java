// @formatter:off
/**
*
* This file is part of streaming pool (http://www.streamingpool.org).
* 
* Copyright (c) 2017-present, CERN. All rights reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
*/
// @formatter:on

package org.streamingpool.ext.tensorics.evaluation;

import java.util.Objects;

import org.streamingpool.core.service.StreamId;

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EvaluationStrategyBuilder {

        private StreamId<?> triggeringStreamId;

        public Builder withTriggeringStreamId(StreamId<?> newTriggeringStreamId) {
            this.triggeringStreamId = newTriggeringStreamId;
            return this;
        }

        @Override
        public EvaluationStrategy build() {
            return triggeredBy(triggeringStreamId);
        }

    }

}
