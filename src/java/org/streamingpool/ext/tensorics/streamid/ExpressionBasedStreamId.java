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
import static org.streamingpool.ext.tensorics.evaluation.EvaluationStrategies.defaultEvaluation;
import static org.tensorics.core.tree.domain.Contexts.newResolvingContext;

import java.io.Serializable;

import org.streamingpool.core.service.StreamId;
import org.streamingpool.ext.tensorics.evaluation.EvaluationStrategy;
import org.streamingpool.ext.tensorics.streamfactory.TensoricsExpressionStreamFactory;
import org.tensorics.core.expressions.Placeholder;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.ResolvingContext;

/**
 * A stream id backed by a tensorics expression. It can be used to request a stream of the expression (resolved) from
 * the streaming pool.
 *
 * @author kfuchsbe, caguiler
 * @param <R> the return type of the expression (and thus the type of the elements of the resulting stream)
 * @see TensoricsExpressionStreamFactory
 */
public class ExpressionBasedStreamId<R> implements StreamId<R>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Expression<R> expression;
    private final ResolvingContext initialContext;
    private final EvaluationStrategy evaluationStrategy;

    public static <R> ExpressionBasedStreamId<R> of(Expression<R> expression, ResolvingContext initialContext,
            EvaluationStrategy evaluationStrategy) {
        return new ExpressionBasedStreamId<>(expression, initialContext, evaluationStrategy);
    }

    public static <R> ExpressionBasedStreamId<R> of(Expression<R> expression) {
        return new ExpressionBasedStreamId<>(expression, newResolvingContext(), defaultEvaluation());
    }

    private ExpressionBasedStreamId(Expression<R> expression, ResolvingContext initialContext,
            EvaluationStrategy evaluationStrategy) {
        this.expression = requireNonNull(expression, "expression must not be null.");
        this.initialContext = requireNonNull(initialContext, "initialContext must not be null.");
        this.evaluationStrategy = requireNonNull(evaluationStrategy, "evaluationStrategy must not be null");

        if (initialContext.resolves(Placeholder.ofClass(EvaluationStrategy.class))) {
            throw new IllegalArgumentException(
                    "The initial context already provides an EvaluationStrategy. This is not allowed, use the parameter");
        }
    }

    public ResolvingContext initialContext() {
        return initialContext;
    }

    public Expression<R> expression() {
        return expression;
    }
    

    public EvaluationStrategy evaluationStrategy() {
        return evaluationStrategy;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evaluationStrategy == null) ? 0 : evaluationStrategy.hashCode());
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + ((initialContext == null) ? 0 : initialContext.hashCode());
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
        if (evaluationStrategy == null) {
            if (other.evaluationStrategy != null) {
                return false;
            }
        } else if (!evaluationStrategy.equals(other.evaluationStrategy)) {
            return false;
        }
        if (expression == null) {
            if (other.expression != null) {
                return false;
            }
        } else if (!expression.equals(other.expression)) {
            return false;
        }
        if (initialContext == null) {
            if (other.initialContext != null) {
                return false;
            }
        } else if (!initialContext.equals(other.initialContext)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ExpressionBasedStreamId [expression=" + expression + ", evaluationStrategy=" + evaluationStrategy
                + ", initialContext=" + initialContext + "]";
    }

    

}
