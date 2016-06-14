/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.streaming.pool.ext.tensorics.service.TensoricsExpressionStreamFactory;

@Configuration
public class TensoricsStreamingConfiguration {

    @Bean
    public TensoricsExpressionStreamFactory tensoricsExpressionStreamFactory() {
        return new TensoricsExpressionStreamFactory();
    }
}
