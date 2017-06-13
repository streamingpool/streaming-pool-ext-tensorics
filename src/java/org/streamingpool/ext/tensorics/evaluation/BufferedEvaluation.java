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

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.service.streamid.BufferSpecification;
import org.streamingpool.core.service.streamid.BufferSpecification.EndStreamMatcher;

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

    public BufferSpecification bufferSpecification() {
        return bufferSpecification;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends EvaluationStrategyBuilder {

        private StreamId<?> startStreamId;
        private Set<BufferSpecification.EndStreamMatcher<?, ?>> endStreamsMatchers = new HashSet<>();
        private Duration timeout = null;

        public Builder withStartStreamId(StreamId<?> newStartStreamId) {
            this.startStreamId = newStartStreamId;
            return this;
        }

        public Builder withEndMatcher(EndStreamMatcher<?, ?> endMatcher) {
            this.endStreamsMatchers.add(endMatcher);
            return this;
        }

        public Builder withTimeout(Duration newTimeout) {
            this.timeout = newTimeout;
            return this;
        }

        @Override
        public EvaluationStrategy build() {
            if (timeout == null) {
                return new BufferedEvaluation(BufferSpecification.ofStartEnd(startStreamId, endStreamsMatchers));
            } else {
                return new BufferedEvaluation(
                        BufferSpecification.ofStartEndTimeout(startStreamId, endStreamsMatchers, timeout));
            }
        }

    }

}
