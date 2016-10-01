/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamid;

import static java.util.Objects.requireNonNull;

import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.evaluation.EvaluationStrategies;
import cern.streaming.pool.ext.tensorics.evaluation.EvaluationStrategy;

/**
 * A {@link StreamId} that encapsulates an {@link Expression} and provides detailed information about its resolution (
 * {@link DetailedResolvedExpression}).
 * 
 * @param <R> the type of the data the source expression resolves
 * @param <E> the type of the expression that is wrapped
 */
public class DetailedExpressionStreamId<R, E extends Expression<R>>
        implements StreamId<DetailedExpressionResult<R, E>> {

    private final E expression;
    private final EvaluationStrategy evaluationStrategy;

    private DetailedExpressionStreamId(E expression, EvaluationStrategy evaluationStrategy) {
        this.expression = requireNonNull(expression, "expression must not be null.");
        this.evaluationStrategy = requireNonNull(evaluationStrategy, "evaluationStrategy must not be null.");
    }

    public static <R, E extends Expression<R>> DetailedExpressionStreamId<R, E> of(E expression) {
        return of(expression, EvaluationStrategies.defaultEvaluation());
    }

    public static <R, E extends Expression<R>> DetailedExpressionStreamId<R, E> of(E expression,
            EvaluationStrategy evaluationStrategy) {
        return new DetailedExpressionStreamId<>(expression, evaluationStrategy);
    }

    public E expression() {
        return expression;
    }

    public EvaluationStrategy evaluationStrategy() {
        return evaluationStrategy;
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
