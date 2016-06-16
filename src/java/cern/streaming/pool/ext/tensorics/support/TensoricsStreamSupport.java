/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.support;

import org.tensorics.core.lang.TensoricScript;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.ReactStream;
import cern.streaming.pool.core.service.support.StreamSupport;
import cern.streaming.pool.core.util.ReactStreams;
import cern.streaming.pool.ext.tensorics.domain.ExpressionBasedStreamId;
import rx.Observable;

/**
 * Support interface for working with tensorics expressions and streams
 * 
 * @author kfuchsbe, caguiler
 */
public interface TensoricsStreamSupport extends StreamSupport {

    default <T> ReactStream<T> discover(Expression<T> expression) {
        return discover(ExpressionBasedStreamId.of(expression));
    }

    default <T> ReactStream<T> discover(TensoricScript<?, T> script) {
        return discover(ExpressionBasedStreamId.of(script.getInternalExpression()));
    }

    default <T> Observable<T> rxFrom(Expression<T> expression) {
        return ReactStreams.rxFrom(discover(expression));
    }

    default <T> Observable<T> rxFrom(TensoricScript<?, T> script) {
        return ReactStreams.rxFrom(discover(script));
    }
}
