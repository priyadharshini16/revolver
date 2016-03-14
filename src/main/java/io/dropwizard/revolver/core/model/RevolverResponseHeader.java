package io.dropwizard.revolver.core.model;

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
public class RevolverResponseHeader {

    private String requestId;

    private String clientIp;

    private long timestamp;

    private long duration;
}
