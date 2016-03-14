package io.dropwizard.revolver.core.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommandHandlerConfig {

    @NotNull
    @NotEmpty
    private String api;

    private HystrixCommandConfig runtime = new HystrixCommandConfig();
}
