/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

/**
 * Implementations of this interface will define how an analysis module will be evaluated. This could e.g. be triggered
 * by some stream event or started and stopped.
 * <p>
 * The implementation of this also will have an influence on how the inputs for the respective calculations will be
 * collected.
 * 
 * @author kfuchsbe
 */
public interface EvaluationStrategy {
    /* marker interface for the moment */
}
