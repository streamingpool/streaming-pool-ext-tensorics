/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.streamingpool.ext.tensorics.streamfactory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.streamingpool.core.service.StreamFactoryRegistry;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.service.streamid.BufferSpecification.EndStreamMatcher;
import org.streamingpool.core.support.RxStreamSupport;
import org.streamingpool.core.testing.AbstractStreamTest;
import org.streamingpool.ext.tensorics.evaluation.BufferedEvaluation;
import org.streamingpool.ext.tensorics.evaluation.EvaluationStrategy;
import org.streamingpool.ext.tensorics.expression.BufferedStreamExpression;
import org.streamingpool.ext.tensorics.streamid.DetailedExpressionStreamId;
import org.tensorics.core.expressions.Placeholder;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.resolve.engine.ResolvingEngines;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;

import io.reactivex.processors.PublishProcessor;
import io.reactivex.subscribers.TestSubscriber;

public class BufferedTensoricsExpressionStreamFactoryStreamTest extends AbstractStreamTest implements RxStreamSupport {

    @Autowired
    private StreamFactoryRegistry registry;

    private BufferedTensoricsExpressionStreamFactory factory;

    private PublishProcessor<String> startPublisher;
    private PublishProcessor<String> endPublisher;
    private PublishProcessor<Integer> sourcePublisher;

    @Before
    public void setUp() {
        factory = new BufferedTensoricsExpressionStreamFactory(ResolvingEngines.defaultEngine());
        registry.addIntercept(factory);

        startPublisher = PublishProcessor.create();
        endPublisher = PublishProcessor.create();
        sourcePublisher = PublishProcessor.create();
    }

    @Test
    public void testSingleBufferedExpressions() {
        TestSubscriber<DetailedExpressionResult<List<Integer>, BufferedStreamExpression<Integer>>> testSubscriber = discoverTensoricExpression();

        startPublisher.onNext("A");
        await();

        sourcePublisher.onNext(1);
        sourcePublisher.onNext(2);
        sourcePublisher.onNext(3);
        sourcePublisher.onNext(4);

        await();
        endPublisher.onNext("A");

        List<Integer> bufferValue = testSubscriber.awaitCount(1).assertValueCount(1).values().get(0).value();
        Assertions.assertThat(bufferValue).containsOnly(1, 2, 3, 4);
    }

    @Test
    public void testOverlappingBufferedExpressions() {
        TestSubscriber<DetailedExpressionResult<List<Integer>, BufferedStreamExpression<Integer>>> testSubscriber = discoverTensoricExpression();

        startPublisher.onNext("A");
        await();

        sourcePublisher.onNext(1);

        await();
        startPublisher.onNext("C");
        await();

        sourcePublisher.onNext(2);

        await();
        startPublisher.onNext("B");
        await();

        sourcePublisher.onNext(3);
        sourcePublisher.onNext(4);

        await();
        endPublisher.onNext("A");
        await();

        sourcePublisher.onNext(5);
        sourcePublisher.onNext(6);

        await();
        endPublisher.onNext("B");
        await();

        sourcePublisher.onNext(7);

        await();
        endPublisher.onNext("C");
        await();

        sourcePublisher.onNext(8);

        List<DetailedExpressionResult<List<Integer>, BufferedStreamExpression<Integer>>> results = testSubscriber
                .awaitCount(3).assertValueCount(3).values();

        List<Integer> bufferA = results.get(0).value();
        List<Integer> bufferB = results.get(1).value();
        List<Integer> bufferC = results.get(2).value();

        assertThat(bufferA).containsOnly(1, 2, 3, 4);
        assertThat(bufferB).containsOnly(3, 4, 5, 6);
        assertThat(bufferC).containsOnly(2, 3, 4, 5, 6, 7);
    }

    private TestSubscriber<DetailedExpressionResult<List<Integer>, BufferedStreamExpression<Integer>>> discoverTensoricExpression() {
        StreamId<?> startStreamId = provide(startPublisher.onBackpressureBuffer()).withUniqueStreamId();
        StreamId<?> endStreamId = provide(endPublisher.onBackpressureBuffer()).withUniqueStreamId();
        StreamId<Integer> sourceStreamId = provide(sourcePublisher.onBackpressureBuffer()).withUniqueStreamId();

        BufferedEvaluation evaluationStrategy = (BufferedEvaluation) BufferedEvaluation.builder()
                .withStartStreamId(startStreamId).withEndMatcher(EndStreamMatcher.endingOnEquals(endStreamId)).build();

        EditableResolvingContext ctx = Contexts.newResolvingContext();
        ctx.put(Placeholder.ofClass(EvaluationStrategy.class), evaluationStrategy);

        BufferedStreamExpression<Integer> bufferedExpression = BufferedStreamExpression.buffer(sourceStreamId);

        DetailedExpressionStreamId<List<Integer>, BufferedStreamExpression<Integer>> rootStreamId = DetailedExpressionStreamId
                .of(bufferedExpression, ctx);

        return rxFrom(rootStreamId).test();
    }

    private void await() {
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            /* */
        }
    }

}
