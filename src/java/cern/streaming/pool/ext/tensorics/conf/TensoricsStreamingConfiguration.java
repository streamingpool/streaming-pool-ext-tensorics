/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.conf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tensorics.core.resolve.engine.ResolvingEngine;

import cern.streaming.pool.ext.tensorics.service.DetailedTensoricsExpressionStreamFactory;
import cern.streaming.pool.ext.tensorics.service.DiscreteFunctionStreamFactory;
import cern.streaming.pool.ext.tensorics.service.TensoricsBufferedStreamFactory;
import cern.streaming.pool.ext.tensorics.service.TensoricsExpressionStreamFactory;

@Configuration
public class TensoricsStreamingConfiguration {

    @Autowired
    private ResolvingEngine resolvingEngine;

    @Bean
    public TensoricsExpressionStreamFactory tensoricsExpressionStreamFactory() {
        return new TensoricsExpressionStreamFactory();
    }

    @Bean
    public DetailedTensoricsExpressionStreamFactory detailedTensoricsExpressionStreamFactory() {
        return new DetailedTensoricsExpressionStreamFactory(resolvingEngine);
    }

    @Bean
    public TensoricsBufferedStreamFactory tensoricsBufferedStreamFactory() {
        return new TensoricsBufferedStreamFactory();
    }

    @Bean
    public DiscreteFunctionStreamFactory discreteFunctionStreamFactory() {
        return new DiscreteFunctionStreamFactory();
    }
}
