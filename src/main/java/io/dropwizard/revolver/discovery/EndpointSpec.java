package io.dropwizard.revolver.discovery;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.revolver.discovery.model.EndpointSpecType;

import javax.validation.constraints.NotNull;

/**
 * @author phaneesh
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
public abstract class EndpointSpec {

    @NotNull
    private final EndpointSpecType type;

    protected EndpointSpec(final EndpointSpecType type) {
        this.type = type;
    }

    public abstract void accept(final SpecVisitor visitor);

    public EndpointSpecType getType() {
        return this.type;
    }
}
