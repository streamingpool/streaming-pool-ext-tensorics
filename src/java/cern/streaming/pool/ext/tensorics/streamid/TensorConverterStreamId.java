/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamid;

import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Tensor;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.streamid.DerivedStreamId;

public class TensorConverterStreamId<T, U> extends DerivedStreamId<Collection<T>, Tensor<U>> {

    public static <T, U, C extends Collection<T>> TensorConverterStreamId<T, U> of(StreamId<C> sourceStreamId,
            Function<T, Position> positionExtractor, Function<T, U> valueMapper) {
        @SuppressWarnings("unchecked") /* Enforced by method signature */
        TensorConverterStreamId<T, U> streamId = new TensorConverterStreamId<>((StreamId<Collection<T>>) sourceStreamId,
                positionExtractor, valueMapper);
        return streamId;
    }

    private TensorConverterStreamId(StreamId<Collection<T>> sourceStreamId, Function<T, Position> positionExtractor,
            Function<T, U> valueMapper) {
        super(sourceStreamId, values -> {
            Map<Position, U> data = values.stream().collect(toMap(positionExtractor, valueMapper));
            Set<Class<?>> dimensions = extractDimensionsAndEnsureConsistency(data);

            return Tensorics.fromMap(dimensions, data);
        });
    }

    private static <U> Set<Class<?>> extractDimensionsAndEnsureConsistency(Map<Position, U> data) {
        Position anyPosition = data.keySet().iterator().next();
        boolean sameDim = data.keySet().stream().map(Position::dimensionSet).allMatch(anyPosition::isConsistentWith);
        if (!sameDim) {
            throw new IllegalArgumentException(
                    "For converting to a tensor, all the positions must have the same dimensions");
        }
        return anyPosition.dimensionSet();
    }

}
