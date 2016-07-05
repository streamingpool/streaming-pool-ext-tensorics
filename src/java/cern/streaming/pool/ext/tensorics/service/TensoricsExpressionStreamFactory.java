/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static cern.streaming.pool.core.service.util.ReactiveStreams.fromRx;
import static cern.streaming.pool.core.service.util.ReactiveStreams.rxFrom;

import org.tensorics.core.resolve.domain.DetailedResolvedExpression;

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
    public <T> ReactiveStream<T> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof ExpressionBasedStreamId)) {
            return null;
        }
        DetailedExpressionStreamId<T,?> expression = ((ExpressionBasedStreamId<T>) id).getDetailedId();
        return fromRx(rxFrom(discoveryService.discover(expression)).map(DetailedResolvedExpression::value));
    }

}
