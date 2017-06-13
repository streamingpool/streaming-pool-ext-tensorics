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

import org.streamingpool.core.service.StreamId;
import org.streamingpool.ext.tensorics.evaluation.EvaluationStrategies;
import org.streamingpool.ext.tensorics.evaluation.EvaluationStrategy;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.ResolvingContext;

/**
 * A {@link StreamId} that encapsulates an {@link Expression} and provides detailed information about its resolution (
 * {@link DetailedExpressionResult}).
 *
 * @param <R> the type of the data the source expression resolves
 * @param <E> the type of the expression that is wrapped
 */
public class DetailedExpressionStreamId<R, E extends Expression<R>>
        implements StreamId<DetailedExpressionResult<R, E>> {

    private final E expression;
    private final EvaluationStrategy evaluationStrategy;
    private final ResolvingContext initialCtx;

    protected DetailedExpressionStreamId(E expression, EvaluationStrategy evaluationStrategy,
            ResolvingContext initialCtx) {
        this.initialCtx = requireNonNull(initialCtx, "initialCtx must not be null.");
        this.expression = requireNonNull(expression, "expression must not be null.");
        this.evaluationStrategy = requireNonNull(evaluationStrategy, "evaluationStrategy must not be null.");
    }

    public static <R, E extends Expression<R>> DetailedExpressionStreamId<R, E> of(E expression) {
        return of(expression, EvaluationStrategies.defaultEvaluation());
    }

    public static <R, E extends Expression<R>> DetailedExpressionStreamId<R, E> of(E expression,
            EvaluationStrategy evaluationStrategy) {
        return new DetailedExpressionStreamId<>(expression, evaluationStrategy, Contexts.newResolvingContext());
    }

    public static <R, E extends Expression<R>> DetailedExpressionStreamId<R, E> of(E expression,
            EvaluationStrategy evaluationStrategy, ResolvingContext initialCtx) {
        return new DetailedExpressionStreamId<>(expression, evaluationStrategy, initialCtx);
    }

    public E expression() {
        return expression;
    }

    public EvaluationStrategy evaluationStrategy() {
        return evaluationStrategy;
    }

    public ResolvingContext initialCtx() {
        return initialCtx;
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

    @Override
    public String toString() {
        return "DetailedExpressionStreamId [expression=" + expression + "]";
    }

}
