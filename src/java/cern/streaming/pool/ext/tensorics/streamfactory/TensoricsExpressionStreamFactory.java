/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamfactory;

import static io.reactivex.Flowable.fromPublisher;

import java.util.Optional;

import org.reactivestreams.Publisher;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;

import cern.streaming.pool.core.service.DiscoveryService;
import cern.streaming.pool.core.service.StreamFactory;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.streamid.DetailedExpressionStreamId;
import cern.streaming.pool.ext.tensorics.streamid.ExpressionBasedStreamId;

/**
 * @author kfuchsbe, caguiler
 */
public class TensoricsExpressionStreamFactory implements StreamFactory {

    @Override
    public <Y> Optional<Publisher<Y>> create(StreamId<Y> id, DiscoveryService discoveryService) {
        if(!(id instanceof ExpressionBasedStreamId)) {
            return Optional.empty();
        }
        ExpressionBasedStreamId<Y> expressionBasedId = (ExpressionBasedStreamId<Y>) id;
        DetailedExpressionStreamId<Y, ?> expression = expressionBasedId.getDetailedId();
        return Optional.of(fromPublisher(discoveryService.discover(expression)).map(DetailedExpressionResult::value));
    }

}
