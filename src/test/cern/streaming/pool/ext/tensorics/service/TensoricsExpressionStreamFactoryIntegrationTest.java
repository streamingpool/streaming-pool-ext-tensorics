/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.service;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.tensorics.core.lang.DoubleTensorics.calculate;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.tensorics.core.function.DiscreteFunction;
import org.tensorics.core.function.lang.FunctionExpressionSupportWithConversion;
import org.tensorics.core.lang.DoubleScript;
import org.tensorics.core.tree.domain.Expression;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.core.service.support.RxStreamSupport;
import cern.streaming.pool.core.testing.AbstractStreamTest;
import cern.streaming.pool.core.util.ReactStreams;
import cern.streaming.pool.ext.tensorics.conf.TensoricsStreamingConfiguration;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import cern.streaming.pool.ext.tensorics.domain.FunctionStreamId;
import cern.streaming.pool.ext.tensorics.domain.StreamIdBasedExpression;
import cern.streaming.pool.ext.tensorics.support.TensoricsStreamSupport;
import rx.Observable;

@ContextConfiguration(classes = TensoricsStreamingConfiguration.class)
public class TensoricsExpressionStreamFactoryIntegrationTest extends AbstractStreamTest
        implements RxStreamSupport, TensoricsStreamSupport {

    private static final StreamId<Double> ID_A = ReactStreams.namedId("a");
    private static final StreamId<Double> ID_B = ReactStreams.namedId("b");

    private static final Expression<Double> A = StreamIdBasedExpression.of(ID_A);
    private static final Expression<Double> B = StreamIdBasedExpression.of(ID_B);
    
    private static StreamId<Pair<Instant, Double>> ID_PAIRS_A = ReactStreams.namedId("VALUES_A");
    
    private static BufferedStreamId<Pair<Instant, Double>> ID_PAIRS_A_BUFFERED = new BufferedStreamId<>(ID_PAIRS_A,
            Duration.of(10, SECONDS));
    
    private static final StreamId<DiscreteFunction<Instant, Double>> ID_PAIRS_A_FUNCTION = new FunctionStreamId<>(
            ID_PAIRS_A_BUFFERED, Pair::getLeft, Pair::getRight);
    
    private static final Expression<DiscreteFunction<Instant, Double>> SIGNAL = StreamIdBasedExpression
            .of(ID_PAIRS_A_FUNCTION);

    @Before
    public void setUp() {
        Observable<Double> oneToTen = Observable.interval(1, TimeUnit.SECONDS).map(Long::doubleValue);
        provide(oneToTen).as(ID_A);

        Observable<Double> tenToTwenty = Observable.interval(1500, TimeUnit.MILLISECONDS).map(Long::doubleValue);
        provide(tenToTwenty).as(ID_B);

        final long startTime = System.currentTimeMillis();

        Observable<Pair<Instant, Double>> valuesWithTimeStamp1 = Observable.interval(1, TimeUnit.SECONDS)
                .map(d -> Pair.of(Instant.ofEpochMilli(startTime + d), d.doubleValue()));

        provide(valuesWithTimeStamp1).as(ID_PAIRS_A);
    }

    @Test
    public void test() throws InterruptedException {

        rxFrom(ID_A).subscribe((a) -> System.out.println("a=" + a));
        rxFrom(ID_B).subscribe((b) -> System.out.println("b=" + b));

        DoubleScript<Boolean> check = new DoubleScript<Boolean>() {

            @Override
            protected Expression<Boolean> describe() {
                Expression<Double> result = calculate(A).plus(B);
                return testIf(result).isLessThan(8.0);
            }
        };

        Observable<Boolean> resultingStream = rxFrom(check);

        DoubleScript<Double> sum = new DoubleScript<Double>() {

            @Override
            protected Expression<Double> describe() {
                return calculate(A).plus(B);
            }
        };

        Observable<Double> sumStream = rxFrom(sum);

        resultingStream.subscribe((res) -> System.out.println("lessThan(8.0)? " + res));
        sumStream.subscribe((res) -> System.out.println("result=" + res));

        Thread.sleep(10000);
    }

    @Test
    public void shortTest() throws InterruptedException {
        rxFrom(calculate(A).plus(B)).subscribe((res) -> System.out.println("result=" + res));
        Thread.sleep(10000);
    }

    @Test
    public void signalExpressionTry() throws InterruptedException {

        DoubleScript<Double> analysis = new DoubleScript<Double>() {

            @Override
            protected Expression<Double> describe() {
                FunctionExpressionSupportWithConversion<Instant, Double> supportWithConversion = withConversion(
                        (Instant t) -> (double) t.toEpochMilli());

                Expression<Double> averageOfF = supportWithConversion.averageOfF(SIGNAL);

                return averageOfF;
            }
        };

        rxFrom(analysis).subscribe((res) -> System.out.println("average=" + res));

        Thread.sleep(50000);
    }

}
