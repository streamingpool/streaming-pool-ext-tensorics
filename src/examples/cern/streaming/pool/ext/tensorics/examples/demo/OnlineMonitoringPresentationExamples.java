/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.examples.demo;

import java.time.Instant;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.tensorics.core.function.DiscreteFunction;
import org.tensorics.core.function.lang.FunctionExpressionSupportWithConversion;
import org.tensorics.core.lang.DoubleScript;
import org.tensorics.core.tree.domain.Expression;

import cern.accsoft.security.rba.login.LoginPolicy;
import cern.rba.util.relogin.RbaLoginService;
import cern.streaming.pool.core.conf.DerivedStreamsConfiguration;
import cern.streaming.pool.core.service.DerivedStreamId;
import cern.streaming.pool.core.testing.AbstractStreamTest;
import cern.streaming.pool.ext.japc.JapcParameterIds;
import cern.streaming.pool.ext.japc.conf.JapcConfiguration;
import cern.streaming.pool.ext.tensorics.conf.TensoricsStreamingConfiguration;
import cern.streaming.pool.ext.tensorics.domain.BufferedStreamId;
import cern.streaming.pool.ext.tensorics.domain.FunctionStreamId;
import cern.streaming.pool.ext.tensorics.domain.StreamIdBasedExpression;
import cern.streaming.pool.ext.tensorics.support.TensoricsStreamSupport;
import rx.Observable;

/**
 * Some examples to be shown during the presentation called 'Steps towards an online monitoring framework'
 * 
 * @author caguiler
 */
@ContextConfiguration(classes = { JapcConfiguration.class, TensoricsStreamingConfiguration.class,
        DerivedStreamsConfiguration.class })
public class OnlineMonitoringPresentationExamples extends AbstractStreamTest implements TensoricsStreamSupport {

    private static final long SLEEP_TIME = 7 * 60 * 1000;

    private static final Expression<DiscreteFunction<Instant, Double>> I_MEAS = StreamIdBasedExpression
            .of(new FunctionStreamId<>(
                    new BufferedStreamId<>(new DerivedStreamId<>(JapcParameterIds.of("RPTE.UA83.RB.A78/SUB_51#I_MEAS"),
                            Helper::failSafeParameterValueToDataPoint)),
                    Pair::getLeft, Pair::getRight));

    private static final Expression<DiscreteFunction<Instant, Double>> U_RES = StreamIdBasedExpression
            .of(new FunctionStreamId<>(new BufferedStreamId<>(
                    new DerivedStreamId<>(JapcParameterIds.of("DQAMGNSRQD.RQD.A78.B23R7/Acquisition"),
                            Helper::failSafeParameterValueToDataPoint)),
                    Pair::getLeft, Pair::getRight));

    @Before
    public void setUp() throws LoginException {
        RbaLoginService rba = new RbaLoginService();
        rba.setLoginPolicy(LoginPolicy.DEFAULT);
        rba.setAutoRefresh(true);
        rba.setRefreshRateSec(3600);
        rba.startAndLogin();
    }

    @Test
    public void continuousRMS() throws InterruptedException {

        DoubleScript<Double> analysisDescribingRMS = new DoubleScript<Double>() {
            @Override
            protected Expression<Double> describe() {
                return rmsOfF(I_MEAS);
            }
        };

        analysisResultAsStream(analysisDescribingRMS).subscribe(res -> System.out.println("rms=" + res));

        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void continuousRMSLessThanAValue() throws InterruptedException {

        DoubleScript<Boolean> analysisDescribingRMS = new DoubleScript<Boolean>() {
            @Override
            protected Expression<Boolean> describe() {

                Expression<Double> rms = rmsOfF(I_MEAS);

                return testIf(rms).isLessThan(0.10);
            }
        };

        analysisResultAsStream(analysisDescribingRMS).subscribe((res) -> System.out.println("rms less than X? = " + res));

        Thread.sleep(SLEEP_TIME);
    }

    private <T> Observable<T> analysisResultAsStream(DoubleScript<T> analysis) {
        return rxFrom(analysis);
    }

    @Test
    public void averageOfSignalDivision() throws InterruptedException {

        DoubleScript<Double> analysis = new DoubleScript<Double>() {
            @Override
            protected Expression<Double> describe() {
                FunctionExpressionSupportWithConversion<Instant, Double> supportWithConversion = withConversion(
                        (Instant t) -> (double) t.toEpochMilli());

                Expression<DiscreteFunction<Instant, Double>> division = supportWithConversion.calculateF(U_RES)
                        .dividedBy(I_MEAS);

                Expression<Double> divisionAverage = supportWithConversion.averageOfF(division);

                return divisionAverage;
            }
        };

        analysisResultAsStream(analysis).subscribe((res) -> System.out.println("avg of U_RES/I_MEAS = " + res));

        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void averageOfSignalDivisionLessThanAValue() throws InterruptedException {

        DoubleScript<Boolean> analysis = new DoubleScript<Boolean>() {
            @Override
            protected Expression<Boolean> describe() {
                FunctionExpressionSupportWithConversion<Instant, Double> supportWithConversion = withConversion(
                        (Instant t) -> (double) t.toEpochMilli());

                Expression<DiscreteFunction<Instant, Double>> division = supportWithConversion.calculateF(U_RES)
                        .dividedBy(I_MEAS);

                Expression<Double> divisionAverage = averageOfF(division);

                return testIf(divisionAverage).isLessThan(2.5e-7);
            }
        };

        analysisResultAsStream(analysis)
                .subscribe((res) -> System.out.println("avg of U_RES/I_MEAS less than X? = " + res));

        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void averageDifference() throws InterruptedException {

        DoubleScript<Double> analysis = new DoubleScript<Double>() {
            @Override
            protected Expression<Double> describe() {
                Expression<Double> avgImeas = averageOfF(I_MEAS);

                Expression<Double> avgUres = averageOfF(U_RES);

                Expression<Double> avgDifference = calculate(avgImeas).minus(avgUres);

                return avgDifference;
            }
        };

        analysisResultAsStream(analysis)
                .subscribe((res) -> System.out.println("avg difference of U_RES and I_MEAS = " + res));

        Thread.sleep(SLEEP_TIME);
    }

    @Test
    public void averageDifferenceLessThanAValue() throws InterruptedException {

        DoubleScript<Boolean> analysis = new DoubleScript<Boolean>() {
            @Override
            protected Expression<Boolean> describe() {

                Expression<Double> avgImeas = averageOfF(I_MEAS);

                Expression<Double> avgUres = averageOfF(U_RES);

                Expression<Double> avgDifference = calculate(avgImeas).minus(avgUres);

                return testIf(avgDifference).isLessThan(0.018);
            }
        };

        analysisResultAsStream(analysis)
                .subscribe((res) -> System.out.println("avg difference of U_RES and I_MEAS less than X? = " + res));

        Thread.sleep(SLEEP_TIME);
    }
}
