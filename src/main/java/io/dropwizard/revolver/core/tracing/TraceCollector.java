package io.dropwizard.revolver.core.tracing;

/**
 * @author phaneesh
 */
public interface TraceCollector {

    void publish(final Trace trace);
}
