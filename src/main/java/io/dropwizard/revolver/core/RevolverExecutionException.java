package io.dropwizard.revolver.core;

/**
 * @author phaneesh
 */
public class RevolverExecutionException extends RuntimeException {
    private final Type type;

    public RevolverExecutionException(final Type type) {
        this.type = type;
    }

    public RevolverExecutionException(final Type type, final String message) {
        super(message);
        this.type = type;
    }

    public RevolverExecutionException(final Type type, final String message, final Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public RevolverExecutionException(final Type type, final Throwable cause) {
        super(cause);
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        SERVICE_ERROR,
        DOWNSTREAM_SERVICE_CALL_FAILURE,
        BAD_REQUEST;
        Type() {
        }
    }
}
