/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cern.streaming.pool.core.service.StreamId;
import cern.streaming.pool.ext.tensorics.evaluation.TriggeredEvaluation;

public class TriggeredEvaluationTest {

    private static final StreamId<?> ANY_STREAM_ID = mock(StreamId.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void nullTriggeringStreamIdThrows() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("triggeringStreamId");
        TriggeredEvaluation.triggeredBy(null);
    }

    @Test
    public void creationWithNonNullStreamIdWorks() {
        TriggeredEvaluation strategy = TriggeredEvaluation.triggeredBy(ANY_STREAM_ID);
        assertThat(strategy).isNotNull();
    }

}
