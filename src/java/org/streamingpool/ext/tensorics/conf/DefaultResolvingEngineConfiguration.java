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

package org.streamingpool.ext.tensorics.conf;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tensorics.core.resolve.engine.ResolvingEngine;
import org.tensorics.core.resolve.engine.ResolvingEngines;
import org.tensorics.core.resolve.resolvers.Resolver;

/**
 * Default configuration for a Tensorics {@link ResolvingEngine}. It collects all the {@link Resolver} Beans in the
 * Spring context. Innclude this class in your configuration if you don't have a {@link ResolvingEngine}.
 */
@Configuration
public class DefaultResolvingEngineConfiguration {

    @Autowired(required=false)
    private List<Resolver<?, ?>> resolvers;

    @Bean
    public ResolvingEngine resolvingEngine() {
        if(resolvers == null) {
            return ResolvingEngines.defaultEngine();
        }
        return ResolvingEngines.defaultEngineWithAdditional(resolvers.stream().toArray(Resolver[]::new));
    }
}
