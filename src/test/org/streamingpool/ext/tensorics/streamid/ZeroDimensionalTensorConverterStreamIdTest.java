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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.support.RxStreamSupport;
import org.streamingpool.core.testing.AbstractStreamTest;
import org.tensorics.core.lang.Tensorics;
import org.tensorics.core.tensor.Tensor;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

public class ZeroDimensionalTensorConverterStreamIdTest extends AbstractStreamTest implements RxStreamSupport {

    @Test
    public void testConvertValueToZeroDimTensor() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        Flowable<Integer> dataStream = Flowable.fromIterable(data);

        StreamId<Integer> dataId = provide(dataStream).withUniqueStreamId();
        ZeroDimensionalTensorConverterStreamId<Integer, Integer> tensorId = ZeroDimensionalTensorConverterStreamId
                .of(dataId);

        List<Tensor<Integer>> values = valuesOf(tensorId);

        assertThat(values).hasSameSizeAs(data);

        List<Integer> tensorsData = values.stream().map(Tensorics::mapFrom).map(Map::values).flatMap(Collection::stream)
                .collect(toList());
        assertThat(tensorsData).containsAll(data);
    }

    private <T> List<T> valuesOf(StreamId<T> streamId) {
        TestSubscriber<T> subscriber = TestSubscriber.create();
        rxFrom(streamId).subscribe(subscriber);
        try {
            subscriber.await();
        } catch (InterruptedException e) {
            fail("Interrupted while waiting for stream completion", e);
        }
        return subscriber.values();
    }
}
