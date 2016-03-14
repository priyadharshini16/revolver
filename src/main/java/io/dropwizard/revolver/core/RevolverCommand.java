package io.dropwizard.revolver.core;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.netflix.hystrix.HystrixCommand;
import io.dropwizard.revolver.core.config.ClientConfig;
import io.dropwizard.revolver.core.config.CommandHandlerConfig;
import io.dropwizard.revolver.core.config.RevolverServiceConfig;
import io.dropwizard.revolver.core.config.RuntimeConfig;
import io.dropwizard.revolver.core.model.RevolverRequest;
import io.dropwizard.revolver.core.model.RevolverResponse;
import io.dropwizard.revolver.core.tracing.Trace;
import io.dropwizard.revolver.core.tracing.TraceCollector;
import io.dropwizard.revolver.core.tracing.TraceInfo;
import io.dropwizard.revolver.core.util.RevolverCommandHelper;
import io.dropwizard.revolver.core.util.RevolverExceptionHelper;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author phaneesh
 */
public abstract class RevolverCommand<RequestType extends RevolverRequest, ResponseType extends RevolverResponse, ContextType extends RevolverContext, ServiceConfigurationType extends RevolverServiceConfig, CommandHandlerConfigType extends CommandHandlerConfig> {

    private final ContextType context;
    private ClientConfig clientConfiguration;
    private final RuntimeConfig runtimeConfig;
    private final ServiceConfigurationType serviceConfiguration;
    private final Map<String, CommandHandlerConfigType> apiConfigurations;
    private final TraceCollector traceCollector;

    public RevolverCommand(final ContextType context, ClientConfig clientConfiguration, RuntimeConfig runtimeConfig, ServiceConfigurationType serviceConfiguration, Map<String, CommandHandlerConfigType> apiConfigurations, final TraceCollector traceCollector) {
        this.context = context;
        this.clientConfiguration = clientConfiguration;
        this.runtimeConfig = runtimeConfig;
        this.serviceConfiguration = serviceConfiguration;
        this.apiConfigurations = apiConfigurations;
        this.traceCollector = traceCollector;
    }

    public ResponseType execute(final RequestType request) throws RevolverExecutionException {
        final RequestType normalizedRequest = RevolverCommandHelper.normalize(request);
        final CommandHandlerConfigType apiConfiguration = this.apiConfigurations.get(request.getApi());
        if (null == apiConfiguration) {
            throw new RevolverExecutionException(RevolverExecutionException.Type.BAD_REQUEST, "No api spec defined for key: " + request.getApi());
        }
        final TraceInfo traceInfo = normalizedRequest.getTrace();
        MDC.put("command", RevolverCommandHelper.getName(request));
        MDC.put("transactionId", traceInfo.getTransactionId());
        MDC.put("requestId", traceInfo.getRequestId());
        MDC.put("parentRequestId", traceInfo.getParentRequestId());
        final Stopwatch watch = Stopwatch.createStarted();
        String errorMessage = null;
        try {
            return (ResponseType) new RevolverCommandHandler(RevolverCommandHelper.setter(this, request.getApi()), this.context, this, normalizedRequest).execute();
        } catch (Throwable t) {
            errorMessage = t.getLocalizedMessage();
            throw new RevolverExecutionException(RevolverExecutionException.Type.SERVICE_ERROR, t);
        } finally {
            this.traceCollector.publish(Trace.builder().caller(this.clientConfiguration.getClientName()).service(this.serviceConfiguration.getService()).api(apiConfiguration.getApi()).duration(watch.stop().elapsed(TimeUnit.MILLISECONDS)).transactionId(traceInfo.getTransactionId()).requestId(traceInfo.getRequestId()).parentRequestId(traceInfo.getParentRequestId()).timestamp(traceInfo.getTimestamp()).attributes(traceInfo.getAttributes()).error(!Strings.isNullOrEmpty(errorMessage)).errorReason(errorMessage).build());
            MDC.remove("command");
            MDC.remove("requestId");
            MDC.remove("transactionId");
            MDC.remove("parentRequestId");
        }
    }

    public CompletableFuture<ResponseType> executeAsync(RequestType request) {
        RequestType normalizedRequest = RevolverCommandHelper.normalize(request);
        final TraceInfo traceInfo = normalizedRequest.getTrace();
        MDC.put("command", RevolverCommandHelper.getName(request));
        MDC.put("transactionId", traceInfo.getTransactionId());
        MDC.put("requestId", traceInfo.getRequestId());
        MDC.put("parentRequestId", traceInfo.getParentRequestId());
        final Stopwatch watch = Stopwatch.createStarted();
        Future<ResponseType> responseFuture = new RevolverCommandHandler(RevolverCommandHelper.setter(this, request.getApi()), this.context, this, normalizedRequest).queue();
        return CompletableFuture.supplyAsync(() -> {
                    String errorMessage = null;
                    try {
                        return responseFuture.get();
                    } catch (Throwable t) {
                        errorMessage = RevolverExceptionHelper.getLeafErrorMessage(t);
                        throw new RevolverExecutionException(RevolverExecutionException.Type.SERVICE_ERROR, String.format("Error executing command %s", RevolverCommandHelper.getName(request)), RevolverExceptionHelper.getLeafThrowable(t));
                    } finally {
                        this.traceCollector.publish(Trace.builder()
                                .caller(this.clientConfiguration.getClientName())
                                .service(this.serviceConfiguration.getService())
                                .api(request.getApi())
                                .duration(watch.stop().elapsed(TimeUnit.MILLISECONDS))
                                .transactionId(traceInfo.getTransactionId())
                                .requestId(traceInfo.getRequestId())
                                .parentRequestId(traceInfo.getParentRequestId())
                                .timestamp(traceInfo.getTimestamp())
                                .attributes(traceInfo.getAttributes())
                                .error(!Strings.isNullOrEmpty(errorMessage))
                                .errorReason(errorMessage).build());
                        MDC.remove("command");
                        MDC.remove("requestId");
                        MDC.remove("transactionId");
                        MDC.remove("parentRequestId");
                    }
                }
        );
    }

    public boolean isFallbackEnabled() {
        return true;
    }

    protected abstract ResponseType execute(final ContextType context, final RequestType request) throws Exception;

    protected abstract ResponseType fallback(final ContextType context, final RequestType request);

    public ClientConfig getClientConfiguration() {
        return this.clientConfiguration;
    }

    public RuntimeConfig getRuntimeConfig() {
        return this.runtimeConfig;
    }

    public ServiceConfigurationType getServiceConfiguration() {
        return this.serviceConfiguration;
    }


    public Map<String, CommandHandlerConfigType> getApiConfigurations() {
        return this.apiConfigurations;
    }

    private static class RevolverCommandHandler<RequestType extends RevolverRequest, ResponseType extends RevolverResponse, ContextType extends RevolverContext, ServiceConfigurationType extends RevolverServiceConfig, CommandHandlerConfigurationType extends CommandHandlerConfig>
            extends HystrixCommand<ResponseType> {
        private final RevolverCommand<RequestType, ResponseType, ContextType, ServiceConfigurationType, CommandHandlerConfigurationType> handler;
        private final RequestType request;
        private final ContextType context;

        public RevolverCommandHandler(HystrixCommand.Setter setter, final ContextType context, RevolverCommand<RequestType, ResponseType, ContextType, ServiceConfigurationType, CommandHandlerConfigurationType> handler, RequestType request) {
            super(setter);
            this.context = context;
            this.handler = handler;
            this.request = request;
        }

        @Override
        protected ResponseType run() throws Exception {
            return this.handler.execute(this.context, this.request);
        }

        @Override
        protected ResponseType getFallback() {
            return this.handler.fallback(this.context, this.request);
        }
    }

}
