/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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
