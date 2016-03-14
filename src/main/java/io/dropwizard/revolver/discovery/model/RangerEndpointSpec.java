package io.dropwizard.revolver.discovery.model;

import io.dropwizard.revolver.discovery.EndpointSpec;
import io.dropwizard.revolver.discovery.SpecVisitor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author phaneesh
 */
public class RangerEndpointSpec extends EndpointSpec {

    @Getter
    @Setter
    private String service;

    @Getter
    @Setter
    private String environment;

    public RangerEndpointSpec() {
        super(EndpointSpecType.ranger_sharded);
    }

    public RangerEndpointSpec(final String service, final String environment) {
        super(EndpointSpecType.ranger_sharded);
        this.service = service;
        this.environment = environment;
    }


    @Override
    public void accept(final SpecVisitor visitor) {
        visitor.visit(this);
    }
}
