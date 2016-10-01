/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

/**
 * This is the simplest way of evaluating an analysis: Any update on any of the inputs will trigger a re-evaluation and
 * the last values of the remaining inputs will be used. This strategy allows only calculations on direct values of the
 * streams (i.e. no buffered values can be used).
 * 
 * @author kfuchsbe
 */
public class ContinuousEvaluation implements EvaluationStrategy {
    /* Nothing to do for the moment */

    private static final ContinuousEvaluation INSTANCE = new ContinuousEvaluation();

    private ContinuousEvaluation() {
        super();
    }

    /**
     * @return an instance of the continuous evaluation strategy
     */
    public static final ContinuousEvaluation instance() {
        return INSTANCE;
    }

    public static final Builder builder() {
        return new Builder();
    }

    public static class Builder extends EvaluationStrategyBuilder {

        @Override
        public EvaluationStrategy build() {
            return instance();
        }
    }

}
