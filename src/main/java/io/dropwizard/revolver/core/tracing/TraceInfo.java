package io.dropwizard.revolver.core.tracing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@Builder
public class TraceInfo {

    private String transactionId;

    private String requestId;

    private String parentRequestId;

    private long timestamp;

    private Map<String, String> attributes;

    public TraceInfo() {
        this.transactionId = UUID.randomUUID().toString();
        this.requestId = this.transactionId;
        this.timestamp = System.currentTimeMillis();
    }
}
