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

package org.streamingpool.ext.tensorics.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.streamingpool.core.service.StreamId;

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
