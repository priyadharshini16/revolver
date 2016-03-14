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
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientConfig {


    @NotNull(message="Please provide the name of your service (the caller)")
    @NotEmpty(message="Please provide the name of your service (the caller)")
    private String clientName;
}
