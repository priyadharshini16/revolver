/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dropwizard.revolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.netflix.hystrix.contrib.codahalemetricspublisher.HystrixCodaHaleMetricsPublisher;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.msgpack.MsgPackBundle;
import io.dropwizard.revolver.aeroapike.AerospikeConnectionManager;
import io.dropwizard.revolver.callback.CallbackHandler;
import io.dropwizard.revolver.core.RevolverExecutionException;
import io.dropwizard.revolver.core.config.AerospikeMailBoxConfig;
import io.dropwizard.revolver.core.config.InMemoryMailBoxConfig;
import io.dropwizard.revolver.core.config.RevolverConfig;
import io.dropwizard.revolver.core.config.RevolverServiceConfig;
import io.dropwizard.revolver.discovery.RevolverServiceResolver;
import io.dropwizard.revolver.discovery.model.RangerEndpointSpec;
import io.dropwizard.revolver.discovery.model.SimpleEndpointSpec;
import io.dropwizard.revolver.exception.RevolverExceptionMapper;
import io.dropwizard.revolver.handler.RevolverCallbackRequestFilter;
import io.dropwizard.revolver.http.RevolverHttpCommand;
import io.dropwizard.revolver.http.auth.BasicAuthConfig;
import io.dropwizard.revolver.http.auth.TokenAuthConfig;
import io.dropwizard.revolver.http.config.RevolverHttpApiConfig;
import io.dropwizard.revolver.http.config.RevolverHttpServiceConfig;
import io.dropwizard.revolver.http.model.ApiPathMap;
import io.dropwizard.revolver.persistence.AeroSpikePersistenceProvider;
import io.dropwizard.revolver.persistence.InMemoryPersistenceProvider;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import io.dropwizard.revolver.resource.RevolverCallbackResource;
import io.dropwizard.revolver.resource.RevolverMailboxResource;
import io.dropwizard.revolver.resource.RevolverMetadataResource;
import io.dropwizard.revolver.resource.RevolverRequestResource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.xml.XmlBundle;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.curator.framework.CuratorFramework;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author phaneesh
 */
@Slf4j
public abstract class RevolverBundle<T extends Configuration> implements ConfiguredBundle<T> {

    private static Map<String, RevolverHttpCommand> httpCommands = new HashMap<>();

    private static MultivaluedMap<String, ApiPathMap> serviceToPathMap = new MultivaluedHashMap<>();

    public static final ObjectMapper msgPackObjectMapper = new ObjectMapper(new MessagePackFactory());

    public static final XmlMapper xmlObjectMapper = new XmlMapper();

    private static RevolverServiceResolver serviceNameResolver = null;

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
        //Reset everything before configuration
        HystrixPlugins.reset();
        registerTypes(bootstrap);
        configureXmlMapper();
        bootstrap.addBundle(new XmlBundle());
        bootstrap.addBundle(new MsgPackBundle());
        bootstrap.addBundle(new AssetsBundle("/revolver/dashboard/", "/revolver/dashboard/", "index.html"));
    }

    @Override
    public void run(final T configuration, final Environment environment) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        //Add metrics publisher
        final HystrixCodaHaleMetricsPublisher metricsPublisher = new HystrixCodaHaleMetricsPublisher(environment.metrics());
        HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
        initializeRevolver(configuration, environment);
        final RevolverConfig revolverConfig = getRevolverConfig(configuration);
        if(Strings.isNullOrEmpty(revolverConfig.getHystrixStreamPath())) {
            environment.getApplicationContext().addServlet(HystrixMetricsStreamServlet.class, "/hystrix.stream");
        } else {
            environment.getApplicationContext().addServlet(HystrixMetricsStreamServlet.class, revolverConfig.getHystrixStreamPath());
        }
        environment.jersey().register(new RevolverExceptionMapper());
        final PersistenceProvider persistenceProvider = getPersistenceProvider(configuration, environment);
        final CallbackHandler callbackHandler = CallbackHandler.builder()
                .persistenceProvider(persistenceProvider)
                .revolverConfig(revolverConfig)
                .build();
        environment.jersey().register(new RevolverCallbackRequestFilter(revolverConfig));
        environment.jersey().register(new RevolverRequestResource(environment.getObjectMapper(), msgPackObjectMapper, xmlObjectMapper, persistenceProvider));
        environment.jersey().register(new RevolverCallbackResource(persistenceProvider, callbackHandler));
        environment.jersey().register(new RevolverMailboxResource(persistenceProvider));
        environment.jersey().register(new RevolverMetadataResource(revolverConfig));
    }


    private void registerTypes(final Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(RevolverHttpServiceConfig.class, "http"));
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(RevolverHttpServiceConfig.class, "https"));
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(BasicAuthConfig.class, "basic"));
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(TokenAuthConfig.class, "token"));
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(SimpleEndpointSpec.class, "simple"));
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(RangerEndpointSpec.class, "ranger_sharded"));
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(InMemoryMailBoxConfig.class, "in_memory"));
        bootstrap.getObjectMapper().registerSubtypes(new NamedType(AerospikeMailBoxConfig.class, "aerospike"));
    }

    private void configureXmlMapper() {
        xmlObjectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        xmlObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        xmlObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlObjectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        xmlObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        xmlObjectMapper.configure(ToXmlGenerator.Feature.WRITE_XML_1_1, true);
    }

    private Map<String, RevolverHttpApiConfig> generateApiConfigMap(final RevolverHttpServiceConfig serviceConfiguration) {
        serviceConfiguration.getApis().forEach(apiConfig -> serviceToPathMap.add(serviceConfiguration.getService(),
                ApiPathMap.builder()
                        .api(apiConfig)
                        .path(generatePathExpression(apiConfig.getPath())).build()));
        final ImmutableMap.Builder<String, RevolverHttpApiConfig> configMapBuilder = ImmutableMap.builder();
        serviceConfiguration.getApis().forEach(apiConfig -> configMapBuilder.put(apiConfig.getApi(), apiConfig));
        return configMapBuilder.build();
    }

    private String generatePathExpression(final String path) {
        return path.replaceAll("\\{(([^/])+\\})", "(([^/])+)");
    }

    public static ApiPathMap matchPath(final String service, final String path) {
        if (serviceToPathMap.containsKey(service)) {
            final val apiMap = serviceToPathMap.get(service).stream().filter(api -> path.matches(api.getPath())).findFirst();
            return apiMap.orElse(null);
        } else {
            return null;
        }
    }

    public static RevolverHttpCommand getHttpCommand(final String service) {
        val command = httpCommands.get(service);
        if (null == command) {
            throw new RevolverExecutionException(RevolverExecutionException.Type.BAD_REQUEST, "No service spec defined for service: " + service);
        }
        return command;
    }

    public static RevolverServiceResolver getServiceNameResolver() {
        return serviceNameResolver;
    }

    public abstract RevolverConfig getRevolverConfig(final T configuration);

    PersistenceProvider getPersistenceProvider(final T configuration, final Environment environment) {
        final RevolverConfig revolverConfig = getRevolverConfig(configuration);
        //Default for avoiding no mailbox config NPE
        if (revolverConfig.getMailBox() == null) {
            return new InMemoryPersistenceProvider();
        }
        switch (revolverConfig.getMailBox().getType()) {
            case "in_memory":
                return new InMemoryPersistenceProvider();
            case "aerospike":
                AerospikeConnectionManager.init((AerospikeMailBoxConfig)revolverConfig.getMailBox());
                return new AeroSpikePersistenceProvider((AerospikeMailBoxConfig)revolverConfig.getMailBox(), environment.getObjectMapper());
        }
        throw new IllegalArgumentException("Invalid mailbox configuration");
    }

    public CuratorFramework getCurator() {
        return null;
    }

    private void initializeRevolver(final T configuration, final Environment environment) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        final RevolverConfig revolverConfig = getRevolverConfig(configuration);
        if(revolverConfig.getServiceResolverConfig() != null) {
            serviceNameResolver = revolverConfig.getServiceResolverConfig().isUseCurator() ? RevolverServiceResolver.usingCurator()
                    .curatorFramework(getCurator())
                    .objectMapper(environment.getObjectMapper())
                    .resolverConfig(revolverConfig.getServiceResolverConfig())
                    .build() : RevolverServiceResolver.builder()
                    .resolverConfig(revolverConfig.getServiceResolverConfig())
                    .objectMapper(environment.getObjectMapper())
                    .build();
        } else {
            serviceNameResolver = RevolverServiceResolver.builder()
                    .objectMapper(environment.getObjectMapper())
                    .build();
        }
        for (final RevolverServiceConfig config : revolverConfig.getServices()) {
            final String type = config.getType();
            switch (type) {
                case "http":
                case "https":
                    final RevolverHttpServiceConfig httpConfig = (RevolverHttpServiceConfig) config;
                    try {
                        httpCommands.put(config.getService(), RevolverHttpCommand.builder()
                                .clientConfiguration(revolverConfig.getClientConfig())
                                .runtimeConfig(revolverConfig.getGlobal())
                                .serviceConfiguration(httpConfig).apiConfigurations(generateApiConfigMap(httpConfig))
                                .serviceResolver(serviceNameResolver)
                                .traceCollector(trace -> {
                                    //TODO: Put in a publisher if required
                                }).build());
                    } catch (ExecutionException e) {
                        log.error("Error creating http command: {}", config.getService(), e);
                    }
                    break;
                default:
                    log.warn("Unsupported Service type: " + type);

            }
        }
    }

}
