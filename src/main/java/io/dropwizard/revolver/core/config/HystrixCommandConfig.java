package io.dropwizard.revolver.core.config;

import io.dropwizard.revolver.core.config.hystrix.CircuitBreakerConfig;
import io.dropwizard.revolver.core.config.hystrix.ThreadPoolConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HystrixCommandConfig {

    private ThreadPoolConfig threadPool = new ThreadPoolConfig();

    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
}
