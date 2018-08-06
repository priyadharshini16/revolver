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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.revolver.RevolverBundle;
import io.dropwizard.revolver.core.config.RevolverConfig;
import lombok.extern.slf4j.Slf4j;

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
    }


    @Override
    public void start() {
        scheduledExecutorService.scheduleWithFixedDelay(this::refreshConfig, 120, revolverConfig.getConfigPollIntervalSeconds(), TimeUnit.SECONDS);
    }

    private void refreshConfig() {
        try {
            log.info("Fetching configuration from dynamic url: {}", revolverConfig.getDynamicConfigUrl());
            T response = objectMapper.readValue(revolverConfig.getDynamicConfigUrl(), configClass);
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
