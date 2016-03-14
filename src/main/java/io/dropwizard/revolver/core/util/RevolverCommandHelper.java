package io.dropwizard.revolver.core.util;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.netflix.hystrix.*;
import io.dropwizard.revolver.core.config.CommandHandlerConfig;
import io.dropwizard.revolver.core.config.RevolverServiceConfig;
import io.dropwizard.revolver.core.config.RuntimeConfig;
import io.dropwizard.revolver.core.config.hystrix.CircuitBreakerConfig;
import io.dropwizard.revolver.core.config.hystrix.MetricsConfig;
import io.dropwizard.revolver.core.config.hystrix.ThreadPoolConfig;
import io.dropwizard.revolver.core.RevolverCommand;
import io.dropwizard.revolver.core.RevolverExecutionException;
import io.dropwizard.revolver.core.model.RevolverRequest;
import io.dropwizard.revolver.core.tracing.TraceInfo;

/**
 * @author phaneesh
 */
public class RevolverCommandHelper {

    public static String getName(RevolverRequest request) {
        return Joiner.on(".").join(request.getService(), request.getApi());
    }

    public static <T extends RevolverRequest> T normalize(T request) {
        if (null == request) {
            throw new RevolverExecutionException(RevolverExecutionException.Type.BAD_REQUEST, "Request cannot be null");
        }
        final TraceInfo traceInfo = request.getTrace();
        if (Strings.isNullOrEmpty(traceInfo.getRequestId())) {
            throw new RevolverExecutionException(RevolverExecutionException.Type.BAD_REQUEST, "Request ID must be passed in span");
        }
        if (Strings.isNullOrEmpty(traceInfo.getTransactionId())) {
            throw new RevolverExecutionException(RevolverExecutionException.Type.BAD_REQUEST, "Transaction ID must be passed");
        }
        if (0L == traceInfo.getTimestamp()) {
            traceInfo.setTimestamp(System.currentTimeMillis());
        }
        return request;
    }

    public static HystrixCommand.Setter setter(final RevolverCommand commandHandler, final String api) {
        final RuntimeConfig runtimeConfig = commandHandler.getRuntimeConfig();
        final RevolverServiceConfig serviceConfiguration = commandHandler.getServiceConfiguration();
        final CommandHandlerConfig config = (CommandHandlerConfig) commandHandler.getApiConfigurations().get(api);
        CircuitBreakerConfig circuitBreakerConfig = runtimeConfig.getCircuitBreaker();
        if (null != config.getRuntime() && null != config.getRuntime().getCircuitBreaker()) {
            circuitBreakerConfig = config.getRuntime().getCircuitBreaker();
        } else if (null != serviceConfiguration.getRuntime() && null != serviceConfiguration.getRuntime().getCircuitBreaker()) {
            circuitBreakerConfig = serviceConfiguration.getRuntime().getCircuitBreaker();
        }
        ThreadPoolConfig threadPoolConfig = runtimeConfig.getThreadPool();
        if (null != config.getRuntime() && null != config.getRuntime().getThreadPool()) {
            threadPoolConfig = config.getRuntime().getThreadPool();
        } else if (null != serviceConfiguration.getRuntime() && null != serviceConfiguration.getRuntime().getThreadPool()) {
            threadPoolConfig = serviceConfiguration.getRuntime().getThreadPool();
        }
        final MetricsConfig metricsConfig = runtimeConfig.getMetrics();
        final String keyName = Joiner.on(".").join(commandHandler.getServiceConfiguration().getService(), api, new Object[0]);
        return HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(serviceConfiguration.getService())).andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionIsolationStrategy(threadPoolConfig.isSemaphoreIsolated() ? HystrixCommandProperties.ExecutionIsolationStrategy.SEMAPHORE : HystrixCommandProperties.ExecutionIsolationStrategy.THREAD).withExecutionIsolationSemaphoreMaxConcurrentRequests(threadPoolConfig.getConcurrency()).withFallbackIsolationSemaphoreMaxConcurrentRequests(threadPoolConfig.getConcurrency()).withFallbackEnabled(commandHandler.isFallbackEnabled()).withCircuitBreakerErrorThresholdPercentage(circuitBreakerConfig.getErrorThresholdPercentage()).withCircuitBreakerRequestVolumeThreshold(circuitBreakerConfig.getNumAcceptableFailuresInTimeWindow()).withCircuitBreakerSleepWindowInMilliseconds(circuitBreakerConfig.getWaitTimeBeforeRetry()).withExecutionTimeoutInMilliseconds(threadPoolConfig.getTimeout()).withMetricsHealthSnapshotIntervalInMilliseconds(metricsConfig.getHealthCheckInterval()).withMetricsRollingPercentileBucketSize(metricsConfig.getPercentileBucketSize()).withMetricsRollingPercentileWindowInMilliseconds(metricsConfig.getPercentileTimeInMillis())).andCommandKey(HystrixCommandKey.Factory.asKey(keyName)).andThreadPoolKey(HystrixThreadPoolKey.Factory.asKey(keyName)).andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter().withCoreSize(threadPoolConfig.getConcurrency()).withMaxQueueSize(threadPoolConfig.getMaxRequestQueueSize()).withQueueSizeRejectionThreshold(threadPoolConfig.getDynamicRequestQueueSize()).withMetricsRollingStatisticalWindowBuckets(metricsConfig.getStatsBucketSize()).withMetricsRollingStatisticalWindowInMilliseconds(metricsConfig.getStatsTimeInMillis()));
    }
}
