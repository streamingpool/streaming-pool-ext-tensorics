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

package org.streamingpool.ext.tensorics.streamid;

import static org.tensorics.core.tensor.stream.TensorStreams.toTensor;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.service.streamid.DerivedStreamId;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Tensor;

public class TensorConverterStreamId<T, U> extends DerivedStreamId<Collection<T>, Tensor<U>> {

    public static <T, U, C extends Collection<T>> TensorConverterStreamId<T, U> of(StreamId<C> sourceStreamId,
            Function<T, Position> positionMapper, Function<T, U> valueMapper, Set<Class<?>> dimensions) {
        @SuppressWarnings("unchecked") /* Enforced by method signature */
        TensorConverterStreamId<T, U> streamId = new TensorConverterStreamId<>((StreamId<Collection<T>>) sourceStreamId,
                positionMapper, valueMapper, dimensions);
        return streamId;
    }

    private TensorConverterStreamId(StreamId<Collection<T>> sourceStreamId, Function<T, Position> positionExtractor,
            Function<T, U> valueMapper, Set<Class<?>> dimensions) {
        super(sourceStreamId, values -> values.stream().collect(toTensor(positionExtractor, valueMapper, dimensions)));
    }
}
