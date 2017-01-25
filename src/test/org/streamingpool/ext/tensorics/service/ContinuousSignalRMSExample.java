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

package org.streamingpool.ext.tensorics.service;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.support.RxStreamSupport;
import org.streamingpool.core.testing.AbstractStreamTest;
import org.streamingpool.core.testing.NamedStreamId;
import org.streamingpool.ext.tensorics.conf.DefaultResolvingEngineConfiguration;
import org.streamingpool.ext.tensorics.conf.TensoricsStreamingConfiguration;
import org.streamingpool.ext.tensorics.expression.StreamIdBasedExpression;
import org.streamingpool.ext.tensorics.streamid.BufferedStreamId;
import org.streamingpool.ext.tensorics.streamid.FunctionStreamId;
import org.streamingpool.ext.tensorics.support.TensoricsStreamSupport;
import org.tensorics.core.function.DiscreteFunction;
import org.tensorics.core.function.lang.FunctionExpressionSupportWithConversionAndComparator;
import org.tensorics.core.lang.DoubleScript;
import org.tensorics.core.tree.domain.Expression;

import io.reactivex.Flowable;

/**
 * An example showing how tensorics expressions and the streaming pool framework are combined in order to perform
 * continuously the rms of an online signal
 * 
 * @author caguiler, kfuchsbe
 */
@ContextConfiguration(classes = { DefaultResolvingEngineConfiguration.class, TensoricsStreamingConfiguration.class })
public class ContinuousSignalRMSExample extends AbstractStreamTest implements RxStreamSupport, TensoricsStreamSupport {

    private static StreamId<Pair<Instant, Double>> ID_VALUES = NamedStreamId.ofName("VALUES_A");

    private static BufferedStreamId<Pair<Instant, Double>> ID_VALUES_BUFFERED = new BufferedStreamId<>(ID_VALUES,
            Duration.of(10, SECONDS));

    private static final StreamId<DiscreteFunction<Instant, Double>> ID_FUNCTION = new FunctionStreamId<>(
            ID_VALUES_BUFFERED, Pair::getLeft, Pair::getRight);

    private static final Expression<DiscreteFunction<Instant, Double>> SIGNAL = StreamIdBasedExpression.of(ID_FUNCTION);

    @Before
    public void setUp() {
        final long startTime = System.currentTimeMillis();

        Flowable<Pair<Instant, Double>> valuesWithTimeStamp1 = Flowable.interval(1, TimeUnit.SECONDS)
                .map(d -> Pair.of(Instant.ofEpochMilli(startTime + d), d.doubleValue()));

        provide(valuesWithTimeStamp1).as(ID_VALUES);
    }

    @Ignore("takes forever")
    @Test
    public void continuousRMS() throws InterruptedException {

        DoubleScript<Double> analysisDescribingRMS = new DoubleScript<Double>() {

            @Override
            protected Expression<Double> describe() {
                FunctionExpressionSupportWithConversionAndComparator<Instant, Double> supportWithConversion = withConversionAndComparator(
                        (Instant t) -> (double) t.toEpochMilli(), Instant::compareTo);

                return supportWithConversion.rmsOfF(SIGNAL);
            }
        };

        rxFrom(analysisDescribingRMS).subscribe((res) -> System.out.println("rms=" + res));
        Thread.sleep(50000);
    }

}
