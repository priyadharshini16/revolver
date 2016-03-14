package io.dropwizard.revolver.discovery;

import io.dropwizard.revolver.core.config.CommandHandlerConfig;
import io.dropwizard.revolver.core.config.RevolverServiceConfig;
import io.dropwizard.revolver.core.config.RuntimeConfig;
import io.dropwizard.revolver.core.RevolverCommand;
import io.dropwizard.revolver.core.RevolverContext;
import io.dropwizard.revolver.core.model.RevolverRequest;
import io.dropwizard.revolver.core.model.RevolverResponse;
import io.dropwizard.revolver.core.tracing.TraceCollector;

/**
 * @author phaneesh
 */
public interface RevolverCommandFactory<RequestType extends RevolverRequest, ResponseType extends RevolverResponse, ContextType extends RevolverContext, ServiceConfigurationType extends RevolverServiceConfig, CommandHandlerConfigurationType extends CommandHandlerConfig> {
    RevolverCommand<RequestType, ResponseType, ContextType, ServiceConfigurationType, CommandHandlerConfigurationType> create(final RuntimeConfig runtimeConfig, final ServiceConfigurationType serviceConfigurationType, final RevolverServiceResolver rangerServiceResolver, final TraceCollector traceCollector);
}
