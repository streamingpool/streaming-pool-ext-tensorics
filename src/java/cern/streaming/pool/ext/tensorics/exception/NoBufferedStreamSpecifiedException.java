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

package cern.streaming.pool.ext.tensorics.exception;

import cern.streaming.pool.core.service.streamid.OverlapBufferStreamId;
import cern.streaming.pool.ext.tensorics.evaluation.EvaluationStrategy;

/**
 * Exception that specifies that a buffered {@link EvaluationStrategy} is defined but there are no buffered streams as
 * input (for example an {@link OverlapBufferStreamId}.
 * 
 * @author acalia
 */
public class NoBufferedStreamSpecifiedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NoBufferedStreamSpecifiedException() {
        super("Evaluation strategy set as buffered, but no buffered streams are found as inputs. Make sure you have at least one buffered stream as input.");
    }

}
