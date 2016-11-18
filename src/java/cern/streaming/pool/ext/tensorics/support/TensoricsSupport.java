/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.support;

import java.util.Collections;
import java.util.List;

import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;

public class TensoricsSupport {

    public static <T> Expression<T> unresolvedEmptyLeaf() {
        return new Expression<T>() {

            @Override
            public List<? extends Node> getChildren() {
                return Collections.emptyList();
            }

            @Override
            public boolean isResolved() {
                return false;
            }

            @Override
            public T get() {
                throw new IllegalStateException("You cannot get value from empty leaf.");
            }
        };
    }

}
