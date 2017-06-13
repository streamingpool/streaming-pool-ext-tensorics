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

package org.streamingpool.ext.tensorics.streamfactory;

import java.util.List;

import org.reactivestreams.Publisher;
import org.streamingpool.core.domain.ErrorStreamPair;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamFactory;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.ext.tensorics.streamid.BufferedStreamId;
import org.streamingpool.ext.tensorics.streamid.FunctionStreamId;
import org.tensorics.core.commons.operations.Conversion;
import org.tensorics.core.function.DiscreteFunction;
import org.tensorics.core.function.MapBackedDiscreteFunction;

import io.reactivex.Flowable;

/**
 * Creates streams of {@link DiscreteFunction}s by means of a {@link FunctionStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
@Deprecated
public class DiscreteFunctionStreamFactory implements StreamFactory {

    /* Safe, manually checked */
    @SuppressWarnings("unchecked")
    @Override
    public <T> ErrorStreamPair<T> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof FunctionStreamId)) {
            return ErrorStreamPair.empty();
        }

        FunctionStreamId<?, ?, ?> functionId = (FunctionStreamId<?, ?, ?>) id;

        return ErrorStreamPair.ofData((Publisher<T>) createFunctionStream(functionId, discoveryService));
    }

    <R, X, Y> Publisher<DiscreteFunction<X, Y>> createFunctionStream(FunctionStreamId<R, X, Y> id,
            DiscoveryService discoveryService) {

        BufferedStreamId<R> sourceStream = id.getSourceStream();
        Conversion<? super R, ? extends X> toX = id.getToX();
        Conversion<? super R, ? extends Y> toY = id.getToY();

        Flowable<List<R>> buffered = Flowable.fromPublisher(discoveryService.discover(sourceStream));

        return buffered.map(iterable -> {
            MapBackedDiscreteFunction.Builder<X, Y> functionBuilder = MapBackedDiscreteFunction.builder();

            for (R t : iterable) {
                functionBuilder.put(toX.apply(t), toY.apply(t));
            }

            return functionBuilder.build();
        });
    }

}
