/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.dropwizard.revolver.handler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.revolver.RevolverBundle;
import io.dropwizard.revolver.core.config.AerospikeMailBoxConfig;
import io.dropwizard.revolver.core.config.InMemoryMailBoxConfig;
import io.dropwizard.revolver.core.config.RevolverConfig;
import io.dropwizard.revolver.discovery.model.RangerEndpointSpec;
import io.dropwizard.revolver.discovery.model.SimpleEndpointSpec;
import io.dropwizard.revolver.http.auth.BasicAuthConfig;
import io.dropwizard.revolver.http.auth.TokenAuthConfig;
import io.dropwizard.revolver.http.config.RevolverHttpServiceConfig;
import io.dropwizard.revolver.http.config.RevolverHttpsServiceConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class DynamicConfigHandler<T> implements Managed {

    private RevolverConfig revolverConfig;

    private Class<T> configClass;

    private ScheduledExecutorService scheduledExecutorService;

    private static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    protected abstract RevolverConfig getRevolverConfig(final T configuration);

    public DynamicConfigHandler(Class<T> configClass,
                                RevolverConfig revolverConfig) {
        this.configClass = configClass;
        this.revolverConfig = revolverConfig;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.registerSubtypes(new NamedType(RevolverHttpServiceConfig.class, "http"));
        objectMapper.registerSubtypes(new NamedType(RevolverHttpsServiceConfig.class, "https"));
        objectMapper.registerSubtypes(new NamedType(BasicAuthConfig.class, "basic"));
        objectMapper.registerSubtypes(new NamedType(TokenAuthConfig.class, "token"));
        objectMapper.registerSubtypes(new NamedType(SimpleEndpointSpec.class, "simple"));
        objectMapper.registerSubtypes(new NamedType(RangerEndpointSpec.class, "ranger_sharded"));
        objectMapper.registerSubtypes(new NamedType(InMemoryMailBoxConfig.class, "in_memory"));
        objectMapper.registerSubtypes(new NamedType(AerospikeMailBoxConfig.class, "aerospike"));
    }


    @Override
    public void start() {
        scheduledExecutorService.scheduleWithFixedDelay(this::refreshConfig, 120, revolverConfig.getConfigPollIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void refreshConfig() {
        try {
            log.info("Fetching configuration from dynamic url: {}", revolverConfig.getDynamicConfigUrl());
            T response = objectMapper.readValue(new URL(revolverConfig.getDynamicConfigUrl()), configClass);
            RevolverBundle.loadServiceConfiguration(getRevolverConfig(response));
        } catch (Exception e) {
            log.error("Error fetching configuration: {}", e);
        }
    }

    @Override
    public void stop() {
        scheduledExecutorService.shutdown();
    }
}
