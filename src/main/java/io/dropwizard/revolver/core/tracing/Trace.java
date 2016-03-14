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
public class Trace {

    private String transactionId;

    private String requestId;

    private String parentRequestId;

    private long timestamp;

    private long duration;

    private String caller;

    private String service;

    private String api;

    private boolean error;

    private String errorReason;

    private Map<String, String> attributes;
}
