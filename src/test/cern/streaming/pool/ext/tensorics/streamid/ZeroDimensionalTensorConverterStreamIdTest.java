/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamid;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.tensorics.core.tensor.Tensor;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.support.RxStreamSupport;
import cern.streaming.pool.core.testing.AbstractStreamTest;
import cern.streaming.pool.core.testing.subscriber.BlockingTestSubscriber;
import rx.Observable;

public class ZeroDimensionalTensorConverterStreamIdTest extends AbstractStreamTest implements RxStreamSupport {

    @Test
    public void testConvertValueToZeroDimTensor() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4);
        Observable<Integer> dataStream = Observable.from(data);

        StreamId<Integer> dataId = provide(dataStream).withUniqueStreamId();
        ZeroDimensionalTensorConverterStreamId<Integer, Integer> tensorId = ZeroDimensionalTensorConverterStreamId
                .of(dataId);

        List<Tensor<Integer>> values = valuesOf(tensorId);

        assertThat(values).hasSameSizeAs(data);

        List<Integer> tensorsData = values.stream().map(Tensor::asMap).map(Map::values).flatMap(Collection::stream)
                .collect(toList());
        assertThat(tensorsData).containsAll(data);
    }

    private <T> List<T> valuesOf(StreamId<T> streamId) {
        BlockingTestSubscriber<T> subscriber = BlockingTestSubscriber.ofName("Subscriber");
        publisherFrom(streamId).subscribe(subscriber);
        subscriber.await();
        return subscriber.getValues();
    }
}
