package io.dropwizard.revolver.http.model;

import com.google.common.collect.Maps;
import io.dropwizard.revolver.core.model.RevolverRequest;
import io.dropwizard.revolver.core.tracing.TraceInfo;
import lombok.Builder;
import lombok.Data;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

/**
 * @author phaneesh
 */
@Data
public class RevolverHttpRequest extends RevolverRequest
{
    private MultivaluedMap<String, String> headers;
    private MultivaluedMap<String, String> queryParams;
    private Map<String, String> pathParams;
    private String path;
    private byte[] body;

    public RevolverHttpRequest() {
        this.headers = new MultivaluedHashMap<String, String>();
        this.queryParams = new MultivaluedHashMap<String, String>();
        this.pathParams = Maps.newHashMap();
        this.setType("http");
    }

    @Builder
    public RevolverHttpRequest(final String service, final String api, final TraceInfo traceInfo, final MultivaluedMap<String, String> headers, final MultivaluedMap<String, String> queryParams, final Map<String, String> pathParams, final String path, final byte[] body) {
        super("http", service, api, traceInfo);
        this.headers = new MultivaluedHashMap<String, String>();
        this.queryParams = new MultivaluedHashMap<String, String>();
        this.pathParams = Maps.newHashMap();
        this.headers = headers;
        this.queryParams = queryParams;
        this.pathParams = pathParams;
        this.body = body;
        this.path = path;
    }
}
