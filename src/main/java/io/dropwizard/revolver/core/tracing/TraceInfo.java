package io.dropwizard.revolver.core.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TraceInfo {

    private String transactionId;

    private String requestId;

    private String parentRequestId;

    private long timestamp;

    private Map<String, String> attributes;
}
