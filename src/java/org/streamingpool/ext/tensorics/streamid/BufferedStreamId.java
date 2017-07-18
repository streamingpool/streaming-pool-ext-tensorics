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

package org.streamingpool.ext.tensorics.streamid;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.streamingpool.core.service.StreamId;

/**
 * Stream id useful to look up a stream of buffered values of <code>R</code>. The buffer length can be tuned by a
 * Duration.
 * <p>
 * If no buffer length is given it is set to 1 second by default.
 *
 * @author caguiler
 * @param <R> type of the values to buffering
 */
@Deprecated
public class BufferedStreamId<R> implements StreamId<List<R>> {
    private static final long serialVersionUID = 1L;

    private final StreamId<R> sourceStream;
    private final Duration windowLength;

    /**
     * Builds a {@link BufferedStreamId}
     *
     * @param sourceStream stream id to buffer
     */
    public BufferedStreamId(StreamId<R> sourceStream) {
        this(sourceStream, Duration.ofMillis(1500));
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sourceStream == null) ? 0 : sourceStream.hashCode());
        result = prime * result + ((windowLength == null) ? 0 : windowLength.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BufferedStreamId other = (BufferedStreamId) obj;
        if (sourceStream == null) {
            if (other.sourceStream != null) {
                return false;
            }
        } else if (!sourceStream.equals(other.sourceStream)) {
            return false;
        }
        if (windowLength == null) {
            if (other.windowLength != null) {
                return false;
            }
        } else if (!windowLength.equals(other.windowLength)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BufferedStreamId [sourceStream=" + sourceStream + ", windowLength=" + windowLength + "]";
    }
}
