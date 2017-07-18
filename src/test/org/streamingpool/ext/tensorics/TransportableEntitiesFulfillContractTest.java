/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.streamingpool.ext.tensorics;

import org.streamingpool.core.service.StreamId;
import org.streamingpool.core.testing.TransportableEntityFulfilled;
import org.tensorics.core.tree.domain.Expression;

public class TransportableEntitiesFulfillContractTest extends TransportableEntityFulfilled {

    public TransportableEntitiesFulfillContractTest() {
        super(PackageReference.packageName(), StreamId.class, Expression.class);
    }

}
