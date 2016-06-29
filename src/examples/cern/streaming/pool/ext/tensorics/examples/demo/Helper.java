/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.examples.demo;

import java.time.Instant;

import org.apache.commons.lang3.tuple.Pair;

import cern.japc.FailSafeParameterValue;
import cern.japc.MapParameterValue;
import cern.japc.SimpleParameterValue;
import cern.japc.spi.value.simple.FloatArrayValue;


public class Helper  {

    public static Pair<Instant, Double> failSafeParameterValueToDataPoint(FailSafeParameterValue parameterValue) {
        return Pair.of(failSafeParameterValueToInstant(parameterValue), failSafeParameterValueToDouble(parameterValue));
    }

    private static Double failSafeParameterValueToDouble(FailSafeParameterValue parameterValue) {

        if (parameterValue.getValue() instanceof SimpleParameterValue) {
            return ((SimpleParameterValue) parameterValue.getValue()).getDouble();
        } else if (parameterValue.getValue() instanceof MapParameterValue) {
            MapParameterValue mapParameterValue = (MapParameterValue) parameterValue.getValue();
            float value = ((FloatArrayValue) mapParameterValue.get("values")).getFloat(0);

            return Double.valueOf(value);
        }
        throw new UnsupportedOperationException("Cannot decode instance of FailSafeParameterValue");
    }

    private static Instant failSafeParameterValueToInstant(FailSafeParameterValue failSafeParameterValue) {
        return Instant.ofEpochMilli(failSafeParameterValue.getHeader().getAcqStampMillis());
    }
}
