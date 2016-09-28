/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.streamid;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static rx.Observable.interval;
import static rx.Observable.just;
import static rx.Observable.merge;
import static rx.Observable.never;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.tensorics.core.tensor.Position;
import org.tensorics.core.tensor.Tensor;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.streamid.BufferSpecification;
import cern.streaming.pool.core.service.streamid.OverlapBufferStreamId;
import cern.streaming.pool.core.support.RxStreamSupport;
import cern.streaming.pool.core.testing.AbstractStreamTest;
import cern.streaming.pool.core.testing.subscriber.BlockingTestSubscriber;
import rx.Observable;

public class TensorConverterStreamIdTest extends AbstractStreamTest implements RxStreamSupport {

    @Test
    public void testTensoricsConversionValues() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4);

        StreamId<List<Integer>> dataStreamId = provide(just(data)).withUniqueStreamId();

        TensorConverterStreamId<Integer, Integer> tensorConverterStreamId = TensorConverterStreamId.of(dataStreamId,
                v -> Position.of(v), identity());

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

        TensorConverterStreamId.of(dummyStreamId, v -> invalidPositions.get(v), identity()).conversion().apply(data);
    }

    @Test
    public void integrationWithOverlappingBufferStreamId() {
        Observable<Long> sourceStream = interval(1, SECONDS).take(6);
        Observable<String> startStream = merge(just("FLAG").delay(1500, MILLISECONDS), never());
        Observable<String> endStream = startStream.delay(5, SECONDS);

        StreamId<Long> sourceId = provide(sourceStream).withUniqueStreamId();
        StreamId<String> startId = provide(startStream).withUniqueStreamId();
        StreamId<String> endId = provide(endStream).withUniqueStreamId();

        OverlapBufferStreamId<Long> bufferId = OverlapBufferStreamId.of(sourceId,
                BufferSpecification.ofStartAndEnd(startId, endId));
        TensorConverterStreamId<Long, Long> tensorId = TensorConverterStreamId.of(bufferId, Position::of, identity());

        List<Tensor<Long>> values = valuesOf(tensorId);

        assertThat(values).hasSize(1);
        assertThat(values.get(0).asMap().values()).containsOnly(1L, 2L, 3L, 4L, 5L);
    }

    private <T> List<T> valuesOf(StreamId<T> streamId) {
        BlockingTestSubscriber<T> subscriber = BlockingTestSubscriber.ofName("Subscriber");
        publisherFrom(streamId).subscribe(subscriber);
        subscriber.await();
        return subscriber.getValues();
    }

}
