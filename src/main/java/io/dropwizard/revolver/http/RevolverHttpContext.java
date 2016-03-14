package io.dropwizard.revolver.http;

import io.dropwizard.revolver.core.RevolverContext;
import okhttp3.OkHttpClient;

/**
 * @author phaneesh
 */
public class RevolverHttpContext extends RevolverContext {

    private OkHttpClient httpClient;
}
