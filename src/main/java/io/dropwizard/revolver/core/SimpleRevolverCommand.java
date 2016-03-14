package io.dropwizard.revolver.core;

import io.dropwizard.revolver.core.config.ClientConfig;
import io.dropwizard.revolver.core.config.CommandHandlerConfig;
import io.dropwizard.revolver.core.config.RevolverServiceConfig;
import io.dropwizard.revolver.core.config.RuntimeConfig;
import io.dropwizard.revolver.core.model.RevolverRequest;
import io.dropwizard.revolver.core.model.RevolverResponse;
import io.dropwizard.revolver.core.tracing.TraceCollector;

import java.util.Collections;

/**
 * @author phaneesh
 */
public abstract class SimpleRevolverCommand<RequestType extends RevolverRequest, ResponseType extends RevolverResponse, ContextType extends RevolverContext, ServiceConfigurationType extends RevolverServiceConfig, CommandHandlerConfigurationType extends CommandHandlerConfig> extends RevolverCommand<RequestType, ResponseType, ContextType, ServiceConfigurationType, CommandHandlerConfigurationType> {
    public SimpleRevolverCommand(final ContextType context, final ClientConfig clientConfiguration, final RuntimeConfig runtimeConfig, final ServiceConfigurationType serviceConfiguration, final CommandHandlerConfigurationType apiConfiguration, final TraceCollector traceCollector) {
        super(context, clientConfiguration, runtimeConfig, serviceConfiguration, Collections.singletonMap(apiConfiguration.getApi(), apiConfiguration), traceCollector);
    }
}

