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
