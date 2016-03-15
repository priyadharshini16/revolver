package io.dropwizard.revolver.core.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type", visible=true)
public class RevolverServiceConfig {

    @NotNull
    @NotBlank
    private String type;

    @NotNull
    @NotBlank
    private String service;

    private HystrixCommandConfig runtime = new HystrixCommandConfig();

    public RevolverServiceConfig(final String type, final String service) {
        this.type = type;
        this.service = service;
    }
}
