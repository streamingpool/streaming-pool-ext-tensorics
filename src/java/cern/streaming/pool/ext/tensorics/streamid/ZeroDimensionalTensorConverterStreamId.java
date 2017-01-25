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

package cern.streaming.pool.ext.tensorics.streamid;

import static java.util.function.Function.identity;

import java.util.function.Function;

import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Context;
import org.tensorics.core.tensor.Tensor;
import org.tensorics.core.tensor.TensorBuilder;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.streamid.DerivedStreamId;

/**
 * {@link DerivedStreamId} that encapsulate a stream of values (T) into a stream of zero-dimensional {@link Tensor}s. It
 * provides to possibility to map the data type before creating the {@link Tensor}.
 * 
 * @author acalia
 * @param <T> the type of the source data stream
 * @param <U> the type of the Tensor values
 */
public class ZeroDimensionalTensorConverterStreamId<T, U> extends DerivedStreamId<T, Tensor<U>> {

    public static <T> ZeroDimensionalTensorConverterStreamId<T, T> of(StreamId<T> sourceStreamId) {
        return new ZeroDimensionalTensorConverterStreamId<>(sourceStreamId, identity(), any -> Context.empty());
    }

    public static <T, U> ZeroDimensionalTensorConverterStreamId<T, U> withValueMapper(StreamId<T> sourceStreamId,
            Function<T, U> valueMapper) {
        return new ZeroDimensionalTensorConverterStreamId<>(sourceStreamId, valueMapper, any -> Context.empty());
    }

    public static <T> ZeroDimensionalTensorConverterStreamId<T, T> withContextMapper(StreamId<T> sourceStreamId,
            Function<T, Context> contextMapper) {
        return new ZeroDimensionalTensorConverterStreamId<>(sourceStreamId, identity(), contextMapper);
    }

    public static <T, U> ZeroDimensionalTensorConverterStreamId<T, U> of(StreamId<T> sourceStreamId,
            Function<T, U> valueMapper, Function<T, Context> contextMapper) {
        return new ZeroDimensionalTensorConverterStreamId<>(sourceStreamId, valueMapper, contextMapper);
    }

    private ZeroDimensionalTensorConverterStreamId(StreamId<T> sourceStreamId, Function<T, U> valueMapper,
            Function<T, Context> contextMapper) {
        super(sourceStreamId, value -> {
            TensorBuilder<U> builder = Tensorics.builder();
            builder.putAt(valueMapper.apply(value));
            builder.setTensorContext(contextMapper.apply(value));
            return builder.build();
        });
    }

}
