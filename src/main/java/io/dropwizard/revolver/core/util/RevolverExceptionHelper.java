package io.dropwizard.revolver.core.util;

/**
 * @author phaneesh
 */
public class RevolverExceptionHelper {

    public static Throwable getLeafThrowable(final Throwable t) {
        Throwable tmp = t;
        for (Throwable current = t; current != null; current = current.getCause()) {
            tmp = current;
        }
        return tmp;
    }

    public static String getLeafErrorMessage(final Throwable t) {
        return RevolverExceptionHelper.getLeafThrowable(t).getMessage();
    }
}
