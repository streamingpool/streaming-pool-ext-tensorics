/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.domain;

import static java.util.Objects.requireNonNull;

import org.tensorics.core.resolve.domain.DetailedResolved;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.StreamId;

public class DetailedExpressionStreamId<R, E extends Expression<R>> implements StreamId<DetailedResolved<R, E>> {

    private final E expression;

    private DetailedExpressionStreamId(E expression) {
        this.expression = requireNonNull(expression, "expression must not be null.");
    }

    public static <R, E extends Expression<R>> DetailedExpressionStreamId<R, E> of(E expression) {
        return new DetailedExpressionStreamId<>(expression);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
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
        DetailedExpressionStreamId<?, ?> other = (DetailedExpressionStreamId<?, ?>) obj;
        if (expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!expression.equals(other.expression)) {
            return false;
        }
        return true;
    }

    public E getExpression() {
        return expression;
    }
}
