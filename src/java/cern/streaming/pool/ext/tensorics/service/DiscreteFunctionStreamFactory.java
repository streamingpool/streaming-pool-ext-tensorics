/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import java.util.List;

import org.tensorics.core.commons.operations.Conversion;
import org.tensorics.core.function.DiscreteFunction;
import org.tensorics.core.function.MapBackedDiscreteFunction;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.util.ReactStreams;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import cern.streaming.pool.ext.tensorics.domain.FunctionStreamId;
import rx.Observable;

/**
 * Creates streams of {@link DiscreteFunction}s by means of a {@link FunctionStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
public class DiscreteFunctionStreamFactory implements StreamFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> ReactStream<T> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof FunctionStreamId)) {
            return null;
        }

        return (ReactStream<T>) createFunctionStream((FunctionStreamId<?, ?, ?>) id, discoveryService);
    }

    <R, X, Y> ReactStream<DiscreteFunction<X, Y>> createFunctionStream(FunctionStreamId<R, X, Y> id,
            DiscoveryService discoveryService) {

        BufferedStreamId<R> sourceStream = id.getSourceStream();
        Conversion<? super R, ? extends X> toX = id.getToX();
        Conversion<? super R, ? extends Y> toY = id.getToY();

        Observable<List<R>> buffered = ReactStreams.rxFrom(discoveryService.discover(sourceStream));

        Observable<DiscreteFunction<X, Y>> functions = buffered.map(iterable -> {

            MapBackedDiscreteFunction.Builder<X, Y> functionBuilder = MapBackedDiscreteFunction.builder();
            
            for (R t : iterable) {
                functionBuilder.put(toX.apply(t), toY.apply(t));
            }

            return functionBuilder.build();
        });

        return ReactStreams.fromRx(functions);
    }
}
