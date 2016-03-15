package io.dropwizard.revolver.core.config.hystrix;

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
public class ThreadPoolConfig {

    private boolean semaphoreIsolated;

    private int concurrency = 10;

    private int maxRequestQueueSize = 100;

    private int dynamicRequestQueueSize = 10;

    private int timeout = 1000;
}
