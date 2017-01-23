/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.support;

import org.reactivestreams.Publisher;
import org.tensorics.core.lang.TensoricScript;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.support.StreamSupport;
import cern.streaming.pool.ext.tensorics.streamid.ExpressionBasedStreamId;
import io.reactivex.Flowable;

/**
 * Support interface for working with tensorics expressions and streams
 * 
 * @author kfuchsbe, caguiler
 */
public interface TensoricsStreamSupport extends StreamSupport {

    default <T> Publisher<T> discover(Expression<T> expression) {
        return discover(ExpressionBasedStreamId.of(expression));
    }

    default <T> Publisher<T> discover(TensoricScript<?, T> script) {
        return discover(ExpressionBasedStreamId.of(script.getInternalExpression()));
    }

    default <T> Flowable<T> rxFrom(Expression<T> expression) {
        return Flowable.fromPublisher(discover(expression));
    }

    default <T> Flowable<T> rxFrom(TensoricScript<?, T> script) {
        return Flowable.fromPublisher(discover(script));
    }
}
