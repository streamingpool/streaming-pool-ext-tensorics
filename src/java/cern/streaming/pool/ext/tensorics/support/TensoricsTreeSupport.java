/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.support;

import java.util.ArrayList;
import java.util.List;

import org.tensorics.core.tree.domain.Expression;
import org.tensorics.core.tree.domain.Node;
import org.tensorics.core.tree.walking.EveryNodeCallback;
import org.tensorics.core.tree.walking.Trees;

/**
 * Class that should be ported to the tensorics package at some point
 *
 * @author astanisz
 */
public class TensoricsTreeSupport {

    public static <C, T extends Expression<?>> List<C> getNodesOfClass(T rootExpression, Class<C> classToFind) {
        List<C> nodesToReturn = new ArrayList<>();
        Trees.walkParentAfterChildren(rootExpression, new EveryNodeCallback() {
            @Override
            public void onEvery(Node node) {
                if (classToFind.isAssignableFrom(node.getClass())) {
                    // if ( node.getClass().isInstance(classToFind)) {
                    nodesToReturn.add(classToFind.cast(node));
                }
            }
        });
        return nodesToReturn;
    }

}
