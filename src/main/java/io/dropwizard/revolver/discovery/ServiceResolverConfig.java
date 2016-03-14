package io.dropwizard.revolver.discovery;

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
public class ServiceResolverConfig {

    private String zkConnectionString;

    private String namespace;


}
