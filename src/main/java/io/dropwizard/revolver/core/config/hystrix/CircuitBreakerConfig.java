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
public class CircuitBreakerConfig {

    private int numAcceptableFailuresInTimeWindow = 20;

    private int waitTimeBeforeRetry = 5000;

    private int errorThresholdPercentage = 50;

}
