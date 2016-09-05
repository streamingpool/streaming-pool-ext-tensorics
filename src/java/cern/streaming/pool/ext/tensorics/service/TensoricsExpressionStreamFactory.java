/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static cern.streaming.pool.core.service.util.ReactiveStreams.fromRx;
import static cern.streaming.pool.core.service.util.ReactiveStreams.rxFrom;

import java.util.Optional;

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
public class TensoricsExpressionStreamFactory implements StreamFactory {

    @Override
    public <Y> Optional<ReactiveStream<Y>> create(StreamId<Y> id, DiscoveryService discoveryService) {
        if(!(id instanceof ExpressionBasedStreamId)) {
            return Optional.empty();
        }
        ExpressionBasedStreamId<Y> expressionBasedId = (ExpressionBasedStreamId<Y>) id;
        DetailedExpressionStreamId<Y, ?> expression = expressionBasedId.getDetailedId();
        return Optional.of(fromRx(rxFrom(discoveryService.discover(expression)).map(DetailedExpressionResult::value)));
    }

}
