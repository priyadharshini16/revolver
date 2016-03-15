package io.dropwizard.revolver.discovery.model;

import io.dropwizard.revolver.discovery.EndpointSpec;
import io.dropwizard.revolver.discovery.SpecVisitor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author phaneesh
 */
public class SimpleEndpointSpec extends EndpointSpec {

    @Getter
    @Setter
    @NotBlank
    private String host;

    @Getter
    @Setter
    @NotBlank
    private int port;

    public SimpleEndpointSpec() {
        super(EndpointSpecType.simple);
    }

    @Override
    public void accept(final SpecVisitor visitor) {
        visitor.visit(this);
    }

    @Builder
    public SimpleEndpointSpec(final EndpointSpecType type, final String host, final int port) {
        super(type);
        this.host = host;
        this.port = port;
    }
}
