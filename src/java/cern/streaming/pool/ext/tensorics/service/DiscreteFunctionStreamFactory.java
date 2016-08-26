/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import java.util.List;

import org.tensorics.core.commons.operations.Conversion;
import org.tensorics.core.function.DiscreteFunction;
import org.tensorics.core.function.MapBackedDiscreteFunction;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.util.ReactiveStreams;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import cern.streaming.pool.ext.tensorics.domain.FunctionStreamId;
import rx.Observable;

/**
 * Creates streams of {@link DiscreteFunction}s by means of a {@link FunctionStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
public class DiscreteFunctionStreamFactory<T, X, Y>
        implements StreamFactory<DiscreteFunction<X, Y>, FunctionStreamId<T, X, Y>> {

    @Override
    public ReactiveStream<DiscreteFunction<X, Y>> create(FunctionStreamId<T, X, Y> id,
            DiscoveryService discoveryService) {
        return createFunctionStream(id, discoveryService);
    }

    @Override
    public boolean canCreate(StreamId<?> id) {
        return id instanceof FunctionStreamId;
    }

    ReactiveStream<DiscreteFunction<X, Y>> createFunctionStream(FunctionStreamId<T, X, Y> id,
            DiscoveryService discoveryService) {

        BufferedStreamId<T> sourceStream = id.getSourceStream();
        Conversion<? super T, ? extends X> toX = id.getToX();
        Conversion<? super T, ? extends Y> toY = id.getToY();

        Observable<List<T>> buffered = ReactiveStreams.rxFrom(discoveryService.discover(sourceStream));

        Observable<DiscreteFunction<X, Y>> functions = buffered.map(iterable -> {

            MapBackedDiscreteFunction.Builder<X, Y> functionBuilder = MapBackedDiscreteFunction.builder();

            for (T t : iterable) {
                functionBuilder.put(toX.apply(t), toY.apply(t));
            }

            return functionBuilder.build();
        });

        return ReactiveStreams.fromRx(functions);
    }
}
