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

package io.dropwizard.revolver.discovery;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.ranger.ServiceFinderBuilders;
import com.flipkart.ranger.finder.sharded.SimpleShardedServiceFinder;
import com.flipkart.ranger.healthcheck.HealthcheckStatus;
import com.flipkart.ranger.model.ServiceNode;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.dropwizard.revolver.discovery.model.Endpoint;
import io.dropwizard.revolver.discovery.model.RangerEndpointSpec;
import io.dropwizard.revolver.discovery.model.SimpleEndpointSpec;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author phaneesh
 */
@Slf4j
public class RevolverServiceResolver {

    private ObjectMapper objectMapper;
    private final boolean discoverEnabled;
    private final CuratorFramework curatorFramework;
    private final ServiceResolverConfig resolverConfig;
    @Getter
    private Map<String, ShardedServiceDiscoveryInfo> serviceFinders = Maps.newConcurrentMap();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Builder
    public RevolverServiceResolver(final ServiceResolverConfig resolverConfig, final ObjectMapper objectMapper) {
        this.resolverConfig = resolverConfig;
        this.objectMapper = objectMapper;
        if (resolverConfig != null) {
            if (!Strings.isNullOrEmpty(resolverConfig.getZkConnectionString())) {
                this.curatorFramework = CuratorFrameworkFactory.builder().connectString(resolverConfig.getZkConnectionString())
                        .namespace(resolverConfig.getNamespace()).retryPolicy(new RetryNTimes(1000, 500)).build();
                this.curatorFramework.start();
                this.discoverEnabled = true;
            } else {
                this.discoverEnabled = false;
                this.curatorFramework = null;
            }
        } else {
            discoverEnabled = false;
            curatorFramework = null;
        }
    }

    @Builder(builderMethodName = "usingCurator")
    public RevolverServiceResolver(final ServiceResolverConfig resolverConfig, final ObjectMapper objectMapper, final CuratorFramework curatorFramework) {
        this.resolverConfig = resolverConfig;
        this.objectMapper = objectMapper;
        this.curatorFramework = curatorFramework;
        this.discoverEnabled = true;
    }

    public Endpoint resolve(final EndpointSpec endpointSpecification) {
        return new SpecResolver(this.discoverEnabled, this.serviceFinders).resolve(endpointSpecification);
    }


    public void register(final EndpointSpec endpointSpecification) {
        endpointSpecification.accept(new SpecVisitor() {

            @Override
            public void visit(final SimpleEndpointSpec simpleEndpointSpecification) {
                log.info("Initialized simple service: " + simpleEndpointSpecification.getHost());
            }

            @Override
            @SuppressWarnings("unchecked")
            public void visit(final RangerEndpointSpec rangerEndpointSpecification) {
                try {
                    final SimpleShardedServiceFinder serviceFinder = (SimpleShardedServiceFinder) ServiceFinderBuilders
                            .shardedFinderBuilder().withCuratorFramework(curatorFramework)
                            .withNamespace(resolverConfig.getNamespace())
                            .withServiceName(rangerEndpointSpecification.getService()).withDeserializer(data -> {
                                        try {
                                            JsonNode nodeInfoRoot = objectMapper.readTree(data);
                                            if (nodeInfoRoot.has("node_data")) {
                                                ServiceNode serviceNode = new ServiceNode(nodeInfoRoot.get("host").asText(), nodeInfoRoot.get("port").asInt(), objectMapper.treeToValue(nodeInfoRoot.get("node_data"), ShardInfo.class));
                                                serviceNode.setHealthcheckStatus(HealthcheckStatus.valueOf(nodeInfoRoot.get("healthcheck_status").asText()));
                                                serviceNode.setLastUpdatedTimeStamp(nodeInfoRoot.get("last_updated_time_stamp").asLong());
                                                return serviceNode;
                                            }
                                            return objectMapper.readValue(data, new TypeReference<ServiceNode<ShardInfo>>() {
                                            });
                                        } catch (IOException e) {
                                            throw new RuntimeException("Error deserializing results", e);
                                        }
                                    }
                            ).build();
                    serviceFinders.put(rangerEndpointSpecification.getService(), ShardedServiceDiscoveryInfo.builder().environment(rangerEndpointSpecification.getEnvironment()).shardFinder(serviceFinder).build());
                    executorService.submit(() -> {
                                try {
                                    log.info("Service finder starting for: " + rangerEndpointSpecification.getService());
                                    serviceFinder.start();
                                } catch (Exception e) {
                                    log.error("Error registering service finder started for: " + rangerEndpointSpecification.getService(), e);
                                }
                                return null;
                            }
                    );
                    log.info("Initialized ZK service: " + rangerEndpointSpecification.getService());
                } catch (Exception e) {
                    log.error("Error registering hander for service: " + rangerEndpointSpecification.getService(), e);
                }
            }
        });
    }

    @Data
    @Builder
    @NoArgsConstructor
    public static final class ShardedServiceDiscoveryInfo {

        private String environment;
        private SimpleShardedServiceFinder<ShardInfo> shardFinder;

        ShardedServiceDiscoveryInfo(final String environment, final SimpleShardedServiceFinder<ShardInfo> shardFinder) {
            this.environment = environment;
            this.shardFinder = shardFinder;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static final class ShardInfo {
        private String environment;
    }

    private static class SpecResolver implements SpecVisitor {
        private Endpoint endpoint;
        private final boolean discoverEnabled;
        private final Map<String, ShardedServiceDiscoveryInfo> serviceFinders;

        private SpecResolver(final boolean discoverEnabled, final Map<String, ShardedServiceDiscoveryInfo> serviceFinders) {
            this.discoverEnabled = discoverEnabled;
            this.serviceFinders = serviceFinders;
        }

        @Override
        public void visit(final SimpleEndpointSpec simpleEndpointSpecification) {
            this.endpoint = Endpoint.builder().host(simpleEndpointSpecification.getHost()).port(simpleEndpointSpecification.getPort()).build();
        }

        @Override
        public void visit(final RangerEndpointSpec rangerEndpointSpecification) {
            if (!this.discoverEnabled) {
                throw new IllegalAccessError("Zookeeper is not initialized in config. Discovery based lookups will not be possible.");
            }
            final SimpleShardedServiceFinder<ShardInfo> finder = this.serviceFinders.get(rangerEndpointSpecification.getService()).getShardFinder();
            final ServiceNode<ShardInfo> node = finder.get(ShardInfo.builder().environment(rangerEndpointSpecification.getEnvironment()).build());
            //Get only the nodes that are healthy
            if (node != null && node.getHealthcheckStatus() == HealthcheckStatus.healthy) {
                this.endpoint = Endpoint.builder().host(node.getHost()).port(node.getPort()).build();
            }
        }

        Endpoint resolve(final EndpointSpec specification) {
            specification.accept(this);
            return this.endpoint;
        }
    }
}
