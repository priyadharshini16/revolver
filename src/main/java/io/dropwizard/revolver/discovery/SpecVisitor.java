package io.dropwizard.revolver.discovery;

import io.dropwizard.revolver.discovery.model.RangerEndpointSpec;
import io.dropwizard.revolver.discovery.model.SimpleEndpointSpec;

/**
 * @author phaneesh
 */
public interface SpecVisitor {

    void visit(final SimpleEndpointSpec simpleEndpointSpec);

    void visit(final RangerEndpointSpec rangerEndpointSpec);
}
