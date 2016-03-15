package io.dropwizard.revolver.core.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.revolver.core.tracing.TraceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "requestBuilder")
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type", visible=true)
public class RevolverRequest {

    @NotNull
    @NotEmpty
    private String type;

    @NotNull
    @NotEmpty
    private String service;

    @NotNull
    @NotEmpty
    private String api;

    @NotNull
    @Valid
    private TraceInfo trace = new TraceInfo();
}
