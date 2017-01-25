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
