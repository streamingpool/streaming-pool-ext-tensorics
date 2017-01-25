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
