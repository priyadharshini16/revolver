package io.dropwizard.revolver.core.config;

import com.google.common.collect.Lists;
import io.dropwizard.revolver.discovery.ServiceResolverConfig;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author phaneesh
 */
@Builder
@AllArgsConstructor
public class RevolverConfig {

    @NotNull
    @Valid
    @Getter
    @Setter
    private ClientConfig clientConfig;

    @NotNull
    @Valid
    @Getter
    @Setter
    private RuntimeConfig global;

    @NotNull
    @Valid
    @Getter
    @Setter
    private ServiceResolverConfig serviceResolverConfig;

    @NotNull
    @NotBlank
    @Getter
    @Setter
    private String hystrixStreamPath;

    @NotNull
    @NotEmpty
    @Valid
    @Getter
    @Setter
    @Singular
    private List<RevolverServiceConfig> services;

    public RevolverConfig() {
        this.global = new RuntimeConfig();
        this.serviceResolverConfig = new ServiceResolverConfig();
        this.hystrixStreamPath = "/hystrix.stream";
        this.services = Lists.newArrayList();
    }
}
