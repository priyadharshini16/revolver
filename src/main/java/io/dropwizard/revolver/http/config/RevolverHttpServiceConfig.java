package io.dropwizard.revolver.http.config;

import io.dropwizard.revolver.core.config.RevolverServiceConfig;
import io.dropwizard.revolver.discovery.EndpointSpec;
import io.dropwizard.revolver.http.auth.AuthConfig;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * @author phaneesh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
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
    @Singular("api")
    private Set<RevolverHttpApiConfig> apis;
    private boolean trackingHeaders;
    private boolean compression;
    private int connectionKeepAliveInMillis = 60000;

    @Builder
    public RevolverHttpServiceConfig(final String type, final String service, final EndpointSpec enpoint, final int connectionPoolSize,
                                     final boolean authEnabled, final boolean secured, final AuthConfig auth, final String keyStorePath,
                                     final String keystorePassword, @Singular("api") final Set<RevolverHttpApiConfig> apis, final boolean trackingHeaders,
                                     final boolean compression, final int connectionKeepAliveInMillis) {
        super(type, service);
        this.endpoint = enpoint;
        this.connectionPoolSize = connectionPoolSize;
        this.authEnabled = authEnabled;
        this.auth = auth;
        this.secured = secured;
        this.keyStorePath = keyStorePath;
        this.keystorePassword = keystorePassword;
        this.apis = apis;
        this.trackingHeaders = trackingHeaders;
        this.compression = compression;
        this.connectionKeepAliveInMillis = connectionKeepAliveInMillis;
    }
}
