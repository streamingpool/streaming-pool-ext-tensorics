/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.support;

import org.tensorics.core.lang.TensoricScript;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.util.ReactiveStreams;
import cern.streaming.pool.core.support.StreamSupport;
import cern.streaming.pool.ext.tensorics.streamid.ExpressionBasedStreamId;
import rx.Observable;

/**
 * Support interface for working with tensorics expressions and streams
 * 
 * @author kfuchsbe, caguiler
 */
public interface TensoricsStreamSupport extends StreamSupport {

    default <T> ReactiveStream<T> discover(Expression<T> expression) {
        return discover(ExpressionBasedStreamId.of(expression));
    }

    default <T> ReactiveStream<T> discover(TensoricScript<?, T> script) {
        return discover(ExpressionBasedStreamId.of(script.getInternalExpression()));
    }

    default <T> Observable<T> rxFrom(Expression<T> expression) {
        return ReactiveStreams.rxFrom(discover(expression));
    }

    default <T> Observable<T> rxFrom(TensoricScript<?, T> script) {
        return ReactiveStreams.rxFrom(discover(script));
    }
}
