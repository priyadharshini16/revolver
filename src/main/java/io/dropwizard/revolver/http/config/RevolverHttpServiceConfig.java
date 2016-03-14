package io.dropwizard.revolver.http.config;

import io.dropwizard.revolver.core.config.RevolverServiceConfig;
import io.dropwizard.revolver.discovery.EndpointSpec;
import io.dropwizard.revolver.http.auth.AuthConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RevolverHttpServiceConfig extends RevolverServiceConfig {

    @NotNull
    @Valid
    private EndpointSpec endpoint;
    private int connectionPoolSize;
    private boolean authEnabled;
    private boolean secured;
    private AuthConfig auth;
    private String keyStorePath;
    private String keystorePassword;
    private Set<RevolverHttpApiConfig> apis;
    private boolean trackingHeaders;
    private boolean compression;
    private int connectionKeepAliveInMillis;
}
