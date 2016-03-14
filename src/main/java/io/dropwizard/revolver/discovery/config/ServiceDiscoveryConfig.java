package io.dropwizard.revolver.discovery.config;

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
public class ServiceDiscoveryConfig {

    private String zkConnectionString;

    private String namespace;
}
