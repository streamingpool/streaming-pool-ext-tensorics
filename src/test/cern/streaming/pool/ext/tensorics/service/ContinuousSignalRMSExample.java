/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.tensorics.core.function.DiscreteFunction;
import org.tensorics.core.function.lang.FunctionExpressionSupportWithConversionAndComparator;
import org.tensorics.core.lang.DoubleScript;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.support.RxStreamSupport;
import cern.streaming.pool.core.testing.AbstractStreamTest;
import cern.streaming.pool.core.testing.NamedStreamId;
import cern.streaming.pool.ext.tensorics.conf.DefaultResolvingEngineConfiguration;
import cern.streaming.pool.ext.tensorics.conf.TensoricsStreamingConfiguration;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import cern.streaming.pool.ext.tensorics.domain.FunctionStreamId;
import cern.streaming.pool.ext.tensorics.domain.StreamIdBasedExpression;
import cern.streaming.pool.ext.tensorics.support.TensoricsStreamSupport;
import rx.Observable;

/**
 * An example showing how tensorics expressions and the streaming pool framework are combined in order to perform
 * continuously the rms of an online signal
 * 
 * @author caguiler, kfuchsbe
 */
@ContextConfiguration(classes = {DefaultResolvingEngineConfiguration.class, TensoricsStreamingConfiguration.class})
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

        Observable<Pair<Instant, Double>> valuesWithTimeStamp1 = Observable.interval(1, TimeUnit.SECONDS)
                .map(d -> Pair.of(Instant.ofEpochMilli(startTime + d), d.doubleValue()));

        provide(valuesWithTimeStamp1).as(ID_VALUES);
    }

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
