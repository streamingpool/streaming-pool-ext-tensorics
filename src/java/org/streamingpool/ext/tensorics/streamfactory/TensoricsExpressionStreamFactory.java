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

package org.streamingpool.ext.tensorics.streamfactory;

import static io.reactivex.Flowable.fromPublisher;

import org.streamingpool.core.domain.ErrorStreamPair;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamFactory;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.ext.tensorics.streamid.DetailedExpressionStreamId;
import org.streamingpool.ext.tensorics.streamid.ExpressionBasedStreamId;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;

/**
 * @author kfuchsbe, caguiler
 */
public class TensoricsExpressionStreamFactory implements StreamFactory {

    @Override
    public <Y> ErrorStreamPair<Y> create(StreamId<Y> id, DiscoveryService discoveryService) {
        if (!(id instanceof ExpressionBasedStreamId)) {
            return ErrorStreamPair.empty();
        }
        ExpressionBasedStreamId<Y> expressionBasedId = (ExpressionBasedStreamId<Y>) id;
        DetailedExpressionStreamId<Y, ?> expression = expressionBasedId.getDetailedId();
        return ErrorStreamPair
                .ofData(fromPublisher(discoveryService.discover(expression)).map(DetailedExpressionResult::value));
    }

}
