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

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static io.reactivex.Flowable.interval;
import static io.reactivex.Flowable.just;
import static io.reactivex.Flowable.merge;
import static io.reactivex.Flowable.never;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.service.streamid.BufferSpecification;
import org.streamingpool.core.service.streamid.OverlapBufferStreamId;
import org.streamingpool.core.service.streamid.BufferSpecification.EndStreamMatcher;
import org.streamingpool.core.support.RxStreamSupport;
import org.streamingpool.core.testing.AbstractStreamTest;
import org.streamingpool.ext.tensorics.streamid.TensorConverterStreamId;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Tensor;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

public class TensorConverterStreamIdTest extends AbstractStreamTest implements RxStreamSupport {

    @Test
    public void testTensoricsConversionValues() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4);

        StreamId<List<Integer>> dataStreamId = provide(just(data)).withUniqueStreamId();

        TensorConverterStreamId<Integer, Integer> tensorConverterStreamId = TensorConverterStreamId.of(dataStreamId,
                Position::of, identity(), Collections.singleton(Integer.class));

        List<Tensor<Integer>> values = valuesOf(tensorConverterStreamId);

        assertThat(values).hasSize(1);
        assertThat(values.get(0).asMap().values()).containsAll(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTensoricsInconsistentPosition() {
        List<Integer> data = asList(0, 1, 2, 3);
        List<Position> invalidPositions = asList(Position.of(1), Position.of(""), Position.of(2), Position.empty());
        StreamId<List<Integer>> dummyStreamId = new StreamId<List<Integer>>() {
            /**/};

        TensorConverterStreamId
                .of(dummyStreamId, invalidPositions::get, identity(), Collections.singleton(Integer.class))
                .conversion().apply(data);
    }

    @Test
    public void integrationWithOverlappingBufferStreamId() {
        Flowable<Long> sourceStream = interval(1, SECONDS).take(6);
        Flowable<String> startStream = merge(just("FLAG").delay(1500, MILLISECONDS), never());
        Flowable<String> endStream = startStream.delay(5, SECONDS);

        StreamId<Long> sourceId = provide(sourceStream).withUniqueStreamId();
        StreamId<String> startId = provide(startStream).withUniqueStreamId();
        StreamId<String> endId = provide(endStream).withUniqueStreamId();

        OverlapBufferStreamId<Long> bufferId = OverlapBufferStreamId.of(sourceId,
                BufferSpecification.ofStartEnd(startId, Collections.singleton(EndStreamMatcher.endingOnEvery(endId))));
        TensorConverterStreamId<Long, Long> tensorId = TensorConverterStreamId.of(bufferId, Position::of, identity(),
                Collections.singleton(Long.class));

        List<Tensor<Long>> values = valuesOf(tensorId);

        assertThat(values).hasSize(1);
        assertThat(values.get(0).asMap().values()).containsOnly(1L, 2L, 3L, 4L, 5L);
    }

    private <T> List<T> valuesOf(StreamId<T> streamId) {
        TestSubscriber<T> subscriber = TestSubscriber.create();
        rxFrom(streamId).subscribe(subscriber);
        try {
            subscriber.await();
        } catch (InterruptedException e) {
            fail("Interrupted while waiting", e);
        }
        return subscriber.values();
    }

}
