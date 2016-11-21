/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.expression;

import static java.util.Objects.requireNonNull;

import org.tensorics.core.tree.domain.ResolvedExpression;

import cern.streaming.pool.core.service.StreamId;

/**
 * An expression which can be used within the tensorics DSL. It represents values which can be looked up from a stream
 * in the streaming pool. It is always a leaf of the expression tree.
 * 
 * @author kfuchsbe, caguiler
 * @param <R> the return type of the expression (and thus the type of the values that the discovered stream will have to
 *            produce)
 */
public class StreamIdBasedExpression<R> extends UnresolvedStreamIdBasedExpression<R> {

    private final StreamId<R> streamId;

    private StreamIdBasedExpression(StreamId<R> streamId) {
        super(ResolvedExpression.of(streamId));
        this.streamId = requireNonNull(streamId, "streamId must not be null.");
    }

    public static <R> StreamIdBasedExpression<R> of(StreamId<R> streamId) {
        return new StreamIdBasedExpression<>(streamId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((streamId == null) ? 0 : streamId.hashCode());
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
        StreamIdBasedExpression<?> other = (StreamIdBasedExpression<?>) obj;
        if (streamId == null) {
            if (other.streamId != null) {
                return false;
            }
        } else if (!streamId.equals(other.streamId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StreamIdBasedExpression [streamId=" + streamId + "]";
    }

}
