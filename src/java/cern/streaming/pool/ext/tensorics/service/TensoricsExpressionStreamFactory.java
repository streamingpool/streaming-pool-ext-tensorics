/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static cern.streaming.pool.core.service.util.ReactiveStreams.fromRx;
import static cern.streaming.pool.core.service.util.ReactiveStreams.rxFrom;

import org.tensorics.core.resolve.domain.DetailedExpressionResult;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.ReactiveStream;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.domain.DetailedExpressionStreamId;
import cern.streaming.pool.ext.tensorics.domain.ExpressionBasedStreamId;

/**
 * @author kfuchsbe, caguiler
 */
public class TensoricsExpressionStreamFactory<R> implements StreamFactory<R, ExpressionBasedStreamId<R>> {

    @Override
    public ReactiveStream<R> create(ExpressionBasedStreamId<R> id, DiscoveryService discoveryService) {
        DetailedExpressionStreamId<R, ?> expression = (id.getDetailedId());
        return fromRx(rxFrom(discoveryService.discover(expression)).map(DetailedExpressionResult::value));
    }

    @Override
    public boolean canCreate(StreamId<?> id) {
        return id instanceof ExpressionBasedStreamId;
    }
}
