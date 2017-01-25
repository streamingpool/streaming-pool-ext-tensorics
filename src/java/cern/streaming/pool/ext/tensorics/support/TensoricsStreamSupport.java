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
