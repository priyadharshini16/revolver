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

package io.dropwizard.revolver.callback;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.revolver.RevolverBundle;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;
import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.core.config.HystrixCommandConfig;
import io.dropwizard.revolver.core.config.RevolverConfig;
import io.dropwizard.revolver.core.config.hystrix.ThreadPoolConfig;
import io.dropwizard.revolver.discovery.EndpointSpec;
import io.dropwizard.revolver.discovery.model.RangerEndpointSpec;
import io.dropwizard.revolver.discovery.model.SimpleEndpointSpec;
import io.dropwizard.revolver.http.RevolverHttpCommand;
import io.dropwizard.revolver.http.RevolversHttpHeaders;
import io.dropwizard.revolver.http.config.RevolverHttpApiConfig;
import io.dropwizard.revolver.http.config.RevolverHttpServiceConfig;
import io.dropwizard.revolver.http.model.RevolverHttpRequest;
import io.dropwizard.revolver.http.model.RevolverHttpResponse;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author phaneesh
 */
@Data
@Builder
@Slf4j
public class CallbackHandler {

    private PersistenceProvider persistenceProvider;

    private RevolverConfig revolverConfig;

    @Data
    @Builder
    @EqualsAndHashCode
    public static class CallbackConfigKey {
        private URI uri;
        private RevolverCallbackRequest callbackRequest;
    }

    private static LoadingCache<CallbackConfigKey, RevolverHttpServiceConfig> clientLoadingCache = CacheBuilder.newBuilder()
            .build(new CacheLoader<CallbackConfigKey, RevolverHttpServiceConfig>() {
                @Override
                public RevolverHttpServiceConfig load(CallbackConfigKey key) throws Exception {
                    return buildConfiguration(key.callbackRequest, key.uri);
                }
            });


    public void handle(final String requestId) {
        final RevolverCallbackRequest request = persistenceProvider.request(requestId);
        if(request == null) {
            log.warn("Invalid request: {}", requestId);
            return;
        }
        RevolverRequestState state = persistenceProvider.requestState(requestId);
        if(state == null) {
            log.warn("Invalid request state: {}", requestId);
            return;
        }
        if(state != RevolverRequestState.RESPONDED) {
            log.warn("Invalid request state {}: {}", state.name(), requestId);
            return;
        }
        if(Strings.isNullOrEmpty(request.getCallbackUri())) {
            log.warn("Invalid callback uri: {}", requestId);
            return;
        }
        try {
            URI uri = new URI(request.getCallbackUri());
            switch (uri.getScheme()) {
                case "https":
                case "http":
                    makeCallback(requestId, uri, request);
                case "ranger":
                    log.warn("Ranger is not supported yet for request: {}", requestId);
                    break;
                default:
                    log.warn("Invalid protocol for request: {}", requestId);
            }
        } catch (Exception e) {
            log.warn("Invalid callback uri {} for request: {}", request.getCallbackUri(), requestId);
        }
    }

    private void makeCallback(final String requestId, final URI uri, final RevolverCallbackRequest callbackRequest) {
        val future = CompletableFuture.supplyAsync(() -> {
            final RevolverCallbackResponse response = persistenceProvider.response(requestId);
            if (response == null) {
                log.warn("Invalid response: {}", requestId);
                return false;
            }
            try {
                final RevolverHttpServiceConfig httpCommandConfig = buildConfiguration(callbackRequest, uri);
                final RevolverHttpCommand httpCommand = buildCommand(httpCommandConfig);
                final MultivaluedMap<String, String> requestHeaders = new MultivaluedHashMap<>();
                response.getHeaders().forEach(requestHeaders::put);
                final RevolverHttpRequest httpRequest = RevolverHttpRequest.builder()
                        .path(uri.getRawPath())
                        .api("callback")
                        .body(response.getBody() == null ? new byte[0] : response.getBody())
                        .headers(requestHeaders)
                        .method(httpCommand.getApiConfigurations().get("callback").getMethods().stream().findFirst().get())
                        .service(httpCommandConfig.getService())
                        .build();
                RevolverHttpResponse httpResponse = httpCommand.execute(httpRequest);
                if(httpResponse.getStatusCode() >= 200 && httpResponse.getStatusCode() <= 210) {
                    return true;
                } else {
                    log.error("Error from callback host: {} | Status Code: {} | Response Body: ", uri.getHost(), httpResponse.getStatusCode(), httpResponse.getBody() != null ? new String(httpResponse.getBody()) : "NONE");
                    return false;
                }
            } catch (MalformedURLException e) {
                log.error("Invalid callback URL: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (IOException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (CertificateException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (NoSuchAlgorithmException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (UnrecoverableKeyException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (KeyStoreException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (KeyManagementException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (ExecutionException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (TimeoutException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            }
        });
        future.thenAccept( result -> {
            if(!result) {
                log.error("Error making callback for request: {}", requestId);
            }
        });
    }

    private static RevolverHttpServiceConfig buildConfiguration(final RevolverCallbackRequest callbackRequest, final URI uri) throws MalformedURLException {
        EndpointSpec endpointSpec = null;
        String apiName = "callback";
        String serviceName = uri.getHost().replace(".", "-");
        String type = null;
        switch (uri.getScheme()) {
            case "https":
            case "http":
                val simpleEndpoint = new SimpleEndpointSpec();
                simpleEndpoint.setHost(uri.getHost());
                simpleEndpoint.setPort((uri.getPort() == 0 || uri.getPort() == -1) ? 80 : uri.getPort());
                endpointSpec = simpleEndpoint;
                type = uri.getScheme();
                break;
            case "ranger": //format for ranger host: environment.service.api
                val rangerEndpoint = new RangerEndpointSpec();
                val discoveryData = uri.getHost().split("\\.");
                if(discoveryData.length != 3) {
                    throw new MalformedURLException("Invalid ranger host format. Accepted format is environment.service.api");
                }
                rangerEndpoint.setEnvironment(discoveryData[0]);
                rangerEndpoint.setService(discoveryData[1]);
                endpointSpec = rangerEndpoint;
                type = "ranger_sharded";
                apiName = discoveryData[2];
        }
        return RevolverHttpServiceConfig.builder()
                .authEnabled(false)
                .connectionPoolSize(10)
                .secured(false)
                .enpoint(endpointSpec)
                .service(serviceName)
                .type(type)
                .api(RevolverHttpApiConfig.configBuilder()
                        .api(apiName)
                        .method(RevolverHttpApiConfig.RequestMethod.valueOf(callbackRequest.getHeaders().get(RevolversHttpHeaders.CALLBACK_METHOD_HEADER).get(0)))
                        .path("")
                        .runtime(HystrixCommandConfig.builder()
                                .threadPool(ThreadPoolConfig.builder()
                                        .concurrency(10).timeout(Integer.parseInt(callbackRequest.getHeaders().get(RevolversHttpHeaders.CALLBACK_TIMEOUT_HEADER).get(0)))
                                        .build())
                                .build()).build()).build();
    }

    private RevolverHttpCommand buildCommand(final RevolverHttpServiceConfig httpConfig) throws CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException, ExecutionException {
        return RevolverHttpCommand.builder()
                .clientConfiguration(revolverConfig.getClientConfig())
                .runtimeConfig(revolverConfig.getGlobal())
                .serviceConfiguration(httpConfig)
                .apiConfigurations(generateApiConfigMap(httpConfig))
                .serviceResolver(RevolverBundle.getServiceNameResolver())
                .traceCollector(trace -> {
                    //TODO: Put in a publisher if required
                }).build();
    }

    private Map<String, RevolverHttpApiConfig> generateApiConfigMap(final RevolverHttpServiceConfig serviceConfiguration) {
        final ImmutableMap.Builder<String, RevolverHttpApiConfig> configMapBuilder = ImmutableMap.builder();
        serviceConfiguration.getApis().forEach(apiConfig -> configMapBuilder.put(apiConfig.getApi(), apiConfig));
        return configMapBuilder.build();
    }
}
