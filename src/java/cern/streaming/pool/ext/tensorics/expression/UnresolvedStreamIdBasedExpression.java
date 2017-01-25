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

package cern.streaming.pool.ext.tensorics.expression;

import java.util.Collections;
import java.util.List;

import org.tensorics.core.tree.domain.AbstractDeferredExpression;
import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;

import cern.streaming.pool.core.service.StreamId;

public class UnresolvedStreamIdBasedExpression<T> extends AbstractDeferredExpression<T> {

    private final Expression<StreamId<T>> streamIdExpression;

    public UnresolvedStreamIdBasedExpression(Expression<StreamId<T>> streamIdExpression) {
        this.streamIdExpression = streamIdExpression;
    }

    public Expression<StreamId<T>> streamIdExpression() {
        return streamIdExpression;
    }

    @Override
    public List<? extends Node> getChildren() {
        return Collections.singletonList(streamIdExpression);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((streamIdExpression == null) ? 0 : streamIdExpression.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UnresolvedStreamIdBasedExpression<?> other = (UnresolvedStreamIdBasedExpression<?>) obj;
        if (streamIdExpression == null) {
            if (other.streamIdExpression != null) {
                return false;
            }
        } else if (!streamIdExpression.equals(other.streamIdExpression)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UnresolvedStreamIdBasedExpression [streamIdExpression=" + streamIdExpression + "]";
    }

}
