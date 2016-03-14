package io.dropwizard.revolver.discovery.model;

import io.dropwizard.revolver.discovery.EndpointSpec;
import io.dropwizard.revolver.discovery.SpecVisitor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author phaneesh
 */
public class SimpleEndpointSpec extends EndpointSpec {

    @Getter
    @Setter
    private String host;

    @Getter
    @Setter
    private int port;

    public SimpleEndpointSpec() {
        super(EndpointSpecType.simple);
    }

    @Override
    public void accept(final SpecVisitor visitor) {
        visitor.visit(this);
    }

    public SimpleEndpointSpec(final String host, final int port) {
        this();
        this.host = host;
        this.port = port;
    }
}
