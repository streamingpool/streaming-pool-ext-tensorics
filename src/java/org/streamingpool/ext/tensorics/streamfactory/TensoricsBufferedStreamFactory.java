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
import static java.util.Optional.of;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.time.Duration;
import java.util.Optional;

import org.reactivestreams.Publisher;
import org.streamingpool.core.domain.ErrorStreamPair;
import org.streamingpool.core.service.DiscoveryService;
import org.streamingpool.core.service.StreamFactory;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.ext.tensorics.streamid.BufferedStreamId;

/**
 * Creates non-overlapping buffers in the form of lists by means of a {@link BufferedStreamId}
 * 
 * @author caguiler, kfuchsbe
 */
@Deprecated
public class TensoricsBufferedStreamFactory implements StreamFactory {

    /* Safe, manually checked */
    @SuppressWarnings("unchecked")
    @Override
    public <T> ErrorStreamPair<T> create(StreamId<T> id, DiscoveryService discoveryService) {
        if (!(id instanceof BufferedStreamId)) {
            return ErrorStreamPair.empty();
        }
        BufferedStreamId<T> bufferedId = (BufferedStreamId<T>) id;

        Publisher<T> sourceStream = discoveryService.discover(bufferedId.getSourceStream());
        Duration windowLength = bufferedId.getWindowLength();

        return ErrorStreamPair
                .ofData((Publisher<T>) fromPublisher(sourceStream).buffer(windowLength.toNanos(), NANOSECONDS));
    }

}
