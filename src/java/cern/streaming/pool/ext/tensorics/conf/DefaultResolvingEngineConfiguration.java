/**
 * Copyright (c) 2016 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.streaming.pool.ext.tensorics.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.resolve.engine.ResolvingEngines;

@Configuration
public class DefaultResolvingEngineConfiguration {

    @Bean
    public ResolvingEngine defaultResolvingEngine() {
        return ResolvingEngines.defaultEngine();
    }
}
