/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.evaluation.BufferedEvaluation;

public class BufferedEvaluationTest {

    private static final StreamId<?> ANY_STREAM_ID = Mockito.mock(StreamId.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void nullStartStreamThrows() {
        expectNpeWithMessage("startStreamId");
        BufferedEvaluation.ofStartAndEnd(null, ANY_STREAM_ID);
    }

    @Test
    public void nullEndStreamIdThrows() {
        expectNpeWithMessage("endStreamId");
        BufferedEvaluation.ofStartAndEnd(ANY_STREAM_ID, null);
    }

    @Test
    public void creationWithValidStreamIdsWorks() {
        BufferedEvaluation strategy = BufferedEvaluation.ofStartAndEnd(ANY_STREAM_ID, ANY_STREAM_ID);
        Assertions.assertThat(strategy).isNotNull();
    }

    private void expectNpeWithMessage(String substring) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(substring);
    }

}
