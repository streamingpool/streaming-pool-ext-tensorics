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

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import org.streamingpool.core.service.StreamId;
import org.tensorics.core.tree.domain.Expression;

/**
 * A stream id backed by a tensorics expression. It can be used to request a stream of the expression (resolved) from
 * the streaming pool.
 *
 * @author kfuchsbe, caguiler
 * @param <R> the return type of the expression (and thus the type of the elements of the resulting stream)
 */
public class ExpressionBasedStreamId<R> implements StreamId<R>, Serializable {
    private static final long serialVersionUID = 1L;

    private final DetailedExpressionStreamId<R, ?> expression;

    private ExpressionBasedStreamId(Expression<R> expression) {
        this.expression = DetailedExpressionStreamId.of(requireNonNull(expression, "expression must not be null."));
    }

    public static <R> ExpressionBasedStreamId<R> of(Expression<R> expression) {
        return new ExpressionBasedStreamId<>(expression);
    }

    public DetailedExpressionStreamId<R, ?> getDetailedId() {
        return expression;
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

    @Override
    public String toString() {
        return "ExpressionBasedStreamId [expression=" + expression + "]";
    }

}
