package io.dropwizard.revolver.http.config;

import io.dropwizard.revolver.core.config.CommandHandlerConfig;
import io.dropwizard.revolver.core.config.HystrixCommandConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "configBuilder")
public class RevolverHttpApiConfig extends CommandHandlerConfig {

    private String path;

    @NotNull
    @NotEmpty
    private RequestMethod method;

    private Set<Integer> acceptableResponseCodes = Collections.emptySet();

    public RevolverHttpApiConfig(final String api, final HystrixCommandConfig runtime, final String path, final RequestMethod method, final Set<Integer> acceptableResponseCodes) {
        super(api, runtime);
        this.path = path;
        this.method = method;
        this.acceptableResponseCodes = acceptableResponseCodes;
    }

    public enum RequestMethod {
        GET,
        POST,
        PUT,
        DELETE,
        HEAD,
        PATCH,
        OPTIONS;
    }
}
