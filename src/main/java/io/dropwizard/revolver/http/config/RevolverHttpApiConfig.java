package io.dropwizard.revolver.http.config;

import io.dropwizard.revolver.core.config.CommandHandlerConfig;
import io.dropwizard.revolver.core.config.HystrixCommandConfig;
import lombok.*;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

/**
 * @author phaneesh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevolverHttpApiConfig extends CommandHandlerConfig {

    private String path;

    @NotNull
    @NotEmpty
    @Singular
    private Set<RequestMethod> methods = Collections.singleton(RequestMethod.GET);

    private Set<Integer> acceptableResponseCodes = Collections.emptySet();

    @Builder(builderMethodName = "configBuilder")
    public RevolverHttpApiConfig(final String api, final HystrixCommandConfig runtime, final String path, @Singular final Set<RequestMethod> methods, final Set<Integer> acceptableResponseCodes) {
        super(api, runtime);
        this.path = path;
        this.methods = methods;
        this.acceptableResponseCodes = acceptableResponseCodes;
    }

    public enum RequestMethod {
        GET,
        POST,
        PUT,
        DELETE,
        HEAD,
        PATCH,
        OPTIONS
    }
}
