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

import static cern.streaming.pool.ext.tensorics.evaluation.TriggeredEvaluation.triggeredBy;
import static cern.streaming.pool.ext.tensorics.streamid.DetailedExpressionStreamId.of;
import static io.reactivex.Flowable.just;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.tensorics.core.resolve.domain.DetailedExpressionResult;
import org.tensorics.core.resolve.engine.ResolvedContextDidNotGrowException;
import org.tensorics.core.tree.domain.AbstractDeferredExpression;
import org.tensorics.core.tree.domain.Contexts;
import org.tensorics.core.tree.domain.EditableResolvingContext;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;

import cern.streaming.pool.core.conf.EmbeddedPoolConfiguration;
import cern.streaming.pool.core.conf.StreamCreatorFactoryConfiguration;
import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.support.AbstractStreamSupport;
import cern.streaming.pool.core.support.RxStreamSupport;
import cern.streaming.pool.ext.tensorics.conf.TestTensoricsEngineConfiguration;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { EmbeddedPoolConfiguration.class, StreamCreatorFactoryConfiguration.class,
        TestTensoricsEngineConfiguration.class })
public class DetailedTensoricsExpressionStreamFactoryInitialContextTest extends AbstractStreamSupport
        implements RxStreamSupport {

    private static final String ANY_STRING = "TEST";

    @Test
    public void testResolvingEngineThrowsIfNotAbleToGetValueFromInitialContext() throws Exception {
        StreamId<Object> triggerStreamId = provide(just(new Object()).delay(1, SECONDS)).withUniqueStreamId();
        Expression<String> expression = unresolvedLeafExpression();

        TestSubscriber<DetailedExpressionResult<String, Expression<String>>> subscriber = new TestSubscriber<>();
        rxFrom(of(expression, triggeredBy(triggerStreamId))).subscribe(subscriber);
        subscriber.awaitTerminalEvent();

        assertThat(subscriber.errors()).hasSize(1);
        assertThat(subscriber.errors().get(0)).isInstanceOf(ResolvedContextDidNotGrowException.class);
    }

    @Test
    public void testInitialContextIsTakenIntoAccountForTheFinalResolving() {
        StreamId<Object> triggerStreamId = provide(just(new Object()).delay(1, SECONDS)).withUniqueStreamId();
        Expression<String> expression = unresolvedLeafExpression();

        EditableResolvingContext initialCtx = Contexts.newResolvingContext();
        initialCtx.put(expression, ANY_STRING);

        DetailedExpressionResult<String, Expression<String>> detailedResult = syncGetFirstValueOf(
                of(expression, triggeredBy(triggerStreamId), initialCtx));

        assertThat(detailedResult.value()).isEqualTo(ANY_STRING);
    }

    private static AbstractDeferredExpression<String> unresolvedLeafExpression() {
        return new AbstractDeferredExpression<String>() {
            @Override
            public List<? extends Node> getChildren() {
                return Collections.emptyList();
            }
        };
    }

    private <T> T syncGetFirstValueOf(StreamId<T> streamId) {
        AtomicReference<T> reference = new AtomicReference<>();
        CountDownLatch sync = new CountDownLatch(1);
        rxFrom(streamId).take(1).subscribe(value -> {
            reference.set(value);
            sync.countDown();
        });
        try {
            sync.await();
        } catch (InterruptedException e) {
            fail("Interrupted", e);
        }
        return reference.get();
    }
}
