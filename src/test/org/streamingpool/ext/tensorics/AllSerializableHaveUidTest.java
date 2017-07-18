/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.streamingpool.ext.tensorics;

import org.streamingpool.ext.tensorics.testing.SerializableHasUid;

public class AllSerializableHaveUidTest extends SerializableHasUid {

    public AllSerializableHaveUidTest() {
        super(PackageReference.packageName());
    }

}
