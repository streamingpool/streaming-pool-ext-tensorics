/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

public class EvaluationStrategies {

    private static final EvaluationStrategy DEFAULT_EVALUATION_STRATEGY = ContinuousEvaluation.instance();

    private EvaluationStrategies() {
        /* only static methods */
    }

    public static final EvaluationStrategy defaultEvaluation() {
        return DEFAULT_EVALUATION_STRATEGY;
    }

}
