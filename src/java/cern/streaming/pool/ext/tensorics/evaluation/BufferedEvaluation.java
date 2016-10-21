/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.streamid.BufferSpecification;
import cern.streaming.pool.core.service.streamid.BufferSpecification.EndStreamMatcher;

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

    public static final Builder builder() {
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
