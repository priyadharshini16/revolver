package io.dropwizard.revolver.core;

/**
 * @author phaneesh
 */
public class RevolverExecutionException extends RuntimeException {
    private final Type type;

    public RevolverExecutionException(Type type) {
        this.type = type;
    }

    public RevolverExecutionException(Type type, String message2) {
        super(message2);
        this.type = type;
    }

    public RevolverExecutionException(Type type, String message2, Throwable cause) {
        super(message2, cause);
        this.type = type;
    }

    public RevolverExecutionException(Type type, Throwable cause) {
        super(cause);
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public static enum Type {
        SERVICE_ERROR,
        DOWNSTREAM_SERVICE_CALL_FAILURE,
        BAD_REQUEST;
        private Type() {
        }
    }
}
