/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.expression;

import java.util.Collections;
import java.util.List;

import org.tensorics.core.tree.domain.AbstractDeferredExpression;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;

import cern.streaming.pool.core.service.StreamId;

public class UnresolvedStreamIdBasedExpression<T> extends AbstractDeferredExpression<T> {

    private final Expression<StreamId<T>> streamIdExpression;

    public UnresolvedStreamIdBasedExpression(Expression<StreamId<T>> streamIdExpression) {
        this.streamIdExpression = streamIdExpression;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnresolvedStreamIdBasedExpression<?> other = (UnresolvedStreamIdBasedExpression<?>) obj;
        if (streamIdExpression == null) {
            if (other.streamIdExpression != null) {
                return false;
            }
        } else if (!streamIdExpression.equals(other.streamIdExpression)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UnresolvedStreamIdBasedExpression [streamIdExpression=" + streamIdExpression + "]";
    }

}
