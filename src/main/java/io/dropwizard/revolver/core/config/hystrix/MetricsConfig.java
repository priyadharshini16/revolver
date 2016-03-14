package io.dropwizard.revolver.core.config.hystrix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetricsConfig {

    @Max(60000L)
    private int statsTimeInMillis = 60000;

    private int statsBucketSize = 100;

    private int healthCheckInterval = 500;

    private int percentileTimeInMillis = 60000;

    private int percentileBucketSize = 100;
}
