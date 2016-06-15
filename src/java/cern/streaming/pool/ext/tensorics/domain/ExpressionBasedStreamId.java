/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.domain;

import static java.util.Objects.requireNonNull;

import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.StreamId;

/**
 * A stream id, which is backed by a tensorics expression. This will be used to request a stream of it from the
 * streaming pool.
 * 
 * @author kfuchsbe
 * @param <R> the return type of the expression (and thus the type of the elements of the resulting stream)
 */
public class ExpressionBasedStreamId<R> implements StreamId<R> {

    private final Expression<R> expression;

    private ExpressionBasedStreamId(Expression<R> expression) {
        super();
        this.expression = requireNonNull(expression, "expression must not be null.");
    }

    public static <R> ExpressionBasedStreamId<R> of(Expression<R> expression) {
        return new ExpressionBasedStreamId<R>(expression);
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
        ExpressionBasedStreamId<?> other = (ExpressionBasedStreamId<?>) obj;
        if (expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!expression.equals(other.expression)) {
            return false;
        }
        return true;
    }

    public Expression<R> expression() {
        return expression;
    }

}
