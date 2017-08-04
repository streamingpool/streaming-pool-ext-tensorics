/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.streamingpool.ext.tensorics.expression;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;

import org.streamingpool.core.service.StreamId;
import org.tensorics.core.tree.domain.AbstractDeferredExpression;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;
import org.tensorics.core.tree.domain.ResolvedExpression;

public class BufferedStreamExpression<T> extends AbstractDeferredExpression<List<T>> {

    private static final long serialVersionUID = 1L;

    public static <T> BufferedStreamExpression<T> buffer(Expression<StreamId<T>> targetStreamId) {
        return new BufferedStreamExpression<>(targetStreamId);
    }

    public static <T> BufferedStreamExpression<T> buffer(StreamId<T> targetStreamId) {
        return new BufferedStreamExpression<>(targetStreamId);
    }

    private final Expression<StreamId<T>> streamIdExpression;

    protected BufferedStreamExpression(StreamId<T> targetStreamId) {
        this.streamIdExpression = ResolvedExpression.of(requireNonNull(targetStreamId, "target stream cannot be null"));
    }

    protected BufferedStreamExpression(Expression<StreamId<T>> targetStreamId) {
        this.streamIdExpression = requireNonNull(targetStreamId, "target stream cannot be null");
    }

    public Expression<StreamId<T>> streamIdExpression() {
        return streamIdExpression;
    }

    @Override
    public List<? extends Node> getChildren() {
        return Collections.singletonList(streamIdExpression);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((streamIdExpression == null) ? 0 : streamIdExpression.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BufferedStreamExpression<?> other = (BufferedStreamExpression<?>) obj;
        if (streamIdExpression == null) {
            if (other.streamIdExpression != null)
                return false;
        } else if (!streamIdExpression.equals(other.streamIdExpression))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BufferedStreamExpression [targetStreamId=" + streamIdExpression + "]";
    }

}
