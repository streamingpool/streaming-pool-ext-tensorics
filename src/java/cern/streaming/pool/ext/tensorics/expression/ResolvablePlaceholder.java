/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.expression;

import org.tensorics.core.tree.domain.Expression;

/**
 * 
 * 
 * @author astanisz 
 * @param <T>
 */
public interface ResolvablePlaceholder<T> {

    /**
     * 
     * @return
     */
    Expression<T> resolvableExpression();
}
