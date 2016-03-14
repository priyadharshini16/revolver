package io.dropwizard.revolver.core.util;

/**
 * @author phaneesh
 */
public class RevolverExceptionHelper {

    public static Throwable getLeafThrowable(Throwable t) {
        Throwable tmp = t;
        for (Throwable current = t; current != null; current = current.getCause()) {
            tmp = current;
        }
        return tmp;
    }

    public static String getLeafErrorMessage(Throwable t) {
        return RevolverExceptionHelper.getLeafThrowable(t).getMessage();
    }
}
