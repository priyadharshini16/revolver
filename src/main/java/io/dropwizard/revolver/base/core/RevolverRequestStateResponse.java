package io.dropwizard.revolver.base.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@Builder
public class RevolverRequestStateResponse {

    private String requestId;

    private String state;
}
