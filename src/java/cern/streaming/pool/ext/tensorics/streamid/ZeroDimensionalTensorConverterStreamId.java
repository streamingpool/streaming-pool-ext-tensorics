/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

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
