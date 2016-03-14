package io.dropwizard.revolver.http;

import com.google.common.base.Strings;
import io.dropwizard.revolver.core.RevolverCommand;
import io.dropwizard.revolver.core.config.ClientConfig;
import io.dropwizard.revolver.core.config.RuntimeConfig;
import io.dropwizard.revolver.core.tracing.TraceCollector;
import io.dropwizard.revolver.core.tracing.TraceInfo;
import io.dropwizard.revolver.core.util.RevolverCommandHelper;
import io.dropwizard.revolver.discovery.RevolverServiceResolver;
import io.dropwizard.revolver.discovery.model.Endpoint;
import io.dropwizard.revolver.http.config.RevolverHttpApiConfig;
import io.dropwizard.revolver.http.config.RevolverHttpServiceConfig;
import io.dropwizard.revolver.http.model.RevolverHttpRequest;
import io.dropwizard.revolver.http.model.RevolverHttpResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.*;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author phaneesh
 */
@Slf4j

public class RevolverHttpCommand extends RevolverCommand<RevolverHttpRequest, RevolverHttpResponse, RevolverHttpContext, RevolverHttpServiceConfig, RevolverHttpApiConfig> {

    public static final String TXN_ID_HEADER = "X-Transaction-ID";
    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String PARENT_REQUEST_ID_HEADER = "X-Parent-Request-ID";
    public static final String TIMESTAMP_HEADER = "X-Request-Timestamp";
    public static final String CLIENT_HEADER = "X-Client-ID";
    private final RevolverServiceResolver serviceResolver;
    private final OkHttpClient client;

    @Builder
    public RevolverHttpCommand(final RuntimeConfig runtimeConfig, final ClientConfig clientConfiguration, final RevolverHttpServiceConfig serviceConfiguration, final Map<String, RevolverHttpApiConfig> apiConfigurations, final TraceCollector traceCollector, final RevolverServiceResolver serviceResolver) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException, UnrecoverableKeyException {
        super(new RevolverHttpContext(), clientConfiguration, runtimeConfig, serviceConfiguration, apiConfigurations, traceCollector);
        (this.serviceResolver = serviceResolver).register(serviceConfiguration.getEndpoint());
        this.client = RevolverHttpClientFactory.buildClient(serviceConfiguration);
    }

    @Override
    public boolean isFallbackEnabled() {
        return false;
    }

    @Override
    protected RevolverHttpResponse execute(RevolverHttpContext context, RevolverHttpRequest request) throws Exception {
        switch (this.getApiConfigurations().get(request.getApi()).getMethod()) {
            case GET: {
                return doGet(request);
            }
            case POST: {
                return doPost(request);
            }
            case PUT: {
                return doPut(request);
            }
            case DELETE: {
                return doDelete(request);
            }
            case HEAD: {
                return doHead(request);
            }
            case OPTIONS: {
                return doOptions(request);
            }
            case PATCH: {
                return doPatch(request);
            }
        }
        return null;
    }

    @Override
    protected RevolverHttpResponse fallback(RevolverHttpContext context, RevolverHttpRequest requestType) {
        log.error("Fallback triggered for command: " + RevolverCommandHelper.getName(requestType));
        return null;
    }

    private RevolverHttpResponse doGet(RevolverHttpRequest request) throws Exception {
        RevolverHttpApiConfig apiConfiguration = this.getApiConfigurations().get(request.getApi());
        Endpoint endpoint = this.serviceResolver.resolve((this.getServiceConfiguration()).getEndpoint());
        HttpUrl url = generateURI(request, apiConfiguration, endpoint);

        Request.Builder httpRequest = new Request.Builder()
                .url(url);
        httpRequest.get();
        if (null != request.getHeaders()) {
            request.getHeaders().forEach((key, values) -> values.forEach(value -> httpRequest.addHeader(key, value)));
        }
        trackingHeaders(request, httpRequest);
        Response response;
        try {
            response = client.newCall(httpRequest.build()).execute();
            return this.getHttpResponse(apiConfiguration, response);
        } catch (Exception e) {
            log.error("Error running HTTP GET call: ", e);
            throw e;
        }
    }

    private RevolverHttpResponse doOptions(RevolverHttpRequest request) throws Exception {
        RevolverHttpApiConfig apiConfiguration = this.getApiConfigurations().get(request.getApi());
        Endpoint endpoint = this.serviceResolver.resolve((this.getServiceConfiguration()).getEndpoint());
        HttpUrl url = generateURI(request, apiConfiguration, endpoint);
        Request.Builder httpRequest = new Request.Builder()
                .url(url);
        httpRequest.method("OPTIONS", null);
        if (null != request.getHeaders()) {
            request.getHeaders().forEach((key, values) -> values.forEach(value -> httpRequest.addHeader(key, value)));
        }
        trackingHeaders(request, httpRequest);
        Response response;
        try {
            response = client.newCall(httpRequest.build()).execute();
            return this.getHttpResponse(apiConfiguration, response);
        } catch (Exception e) {
            log.error("Error running HTTP GET call: ", e);
            throw e;
        }
    }

    private RevolverHttpResponse doHead(RevolverHttpRequest request) throws Exception {
        RevolverHttpApiConfig apiConfiguration = this.getApiConfigurations().get(request.getApi());
        Endpoint endpoint = this.serviceResolver.resolve((this.getServiceConfiguration()).getEndpoint());
        HttpUrl url = generateURI(request, apiConfiguration, endpoint);
        Request.Builder httpRequest = new Request.Builder()
                .url(url);
        httpRequest.head();
        if (null != request.getHeaders()) {
            request.getHeaders().forEach((key, values) -> values.forEach(value -> httpRequest.addHeader(key, value)));
        }
        trackingHeaders(request, httpRequest);
        Response response;
        try {
            response = client.newCall(httpRequest.build()).execute();
            return this.getHttpResponse(apiConfiguration, response);
        } catch (Exception e) {
            log.error("Error running HTTP GET call: ", e);
            throw e;
        }
    }

    private RevolverHttpResponse doDelete(RevolverHttpRequest request) throws Exception {
        RevolverHttpApiConfig apiConfiguration = this.getApiConfigurations().get(request.getApi());
        Endpoint endpoint = this.serviceResolver.resolve((this.getServiceConfiguration()).getEndpoint());
        HttpUrl url = generateURI(request, apiConfiguration, endpoint);
        Request.Builder httpRequest = new Request.Builder()
                .url(url);
        httpRequest.delete();
        if (null != request.getHeaders()) {
            request.getHeaders().forEach((key, values) -> values.forEach(value -> httpRequest.addHeader(key, value)));
        }
        trackingHeaders(request, httpRequest);
        Response response;
        try {
            response = client.newCall(httpRequest.build()).execute();
            return this.getHttpResponse(apiConfiguration, response);
        } catch (Exception e) {
            log.error("Error running HTTP GET call: ", e);
            throw e;
        }
    }

    private RevolverHttpResponse doPatch(RevolverHttpRequest request) throws Exception {
        RevolverHttpApiConfig apiConfiguration = this.getApiConfigurations().get(request.getApi());
        Endpoint endpoint = this.serviceResolver.resolve((this.getServiceConfiguration()).getEndpoint());
        HttpUrl url = generateURI(request, apiConfiguration, endpoint);
        Request.Builder httpRequest = new Request.Builder()
                .url(url);
        httpRequest.patch(RequestBody.create(MediaType.parse(request.getHeaders().getFirst("Content-Type")), request.getBody()));
        if (null != request.getHeaders()) {
            request.getHeaders().forEach((key, values) -> values.forEach(value -> httpRequest.addHeader(key, value)));
        }
        trackingHeaders(request, httpRequest);
        Response response;
        try {
            response = client.newCall(httpRequest.build()).execute();
            return this.getHttpResponse(apiConfiguration, response);
        } catch (Exception e) {
            log.error("Error running HTTP GET call: ", e);
            throw e;
        }
    }

    private RevolverHttpResponse doPost(RevolverHttpRequest request) throws Exception {
        RevolverHttpApiConfig apiConfiguration = this.getApiConfigurations().get(request.getApi());
        Endpoint endpoint = this.serviceResolver.resolve((this.getServiceConfiguration()).getEndpoint());
        HttpUrl url = generateURI(request, apiConfiguration, endpoint);
        Request.Builder httpRequest = new Request.Builder()
                .url(url);
        httpRequest.post(RequestBody.create(MediaType.parse(request.getHeaders().getFirst("Content-Type")), request.getBody()));
        if (null != request.getHeaders()) {
            request.getHeaders().forEach((key, values) -> values.forEach(value -> httpRequest.addHeader(key, value)));
        }
        trackingHeaders(request, httpRequest);
        Response response;
        try {
            response = client.newCall(httpRequest.build()).execute();
            return this.getHttpResponse(apiConfiguration, response);
        } catch (Exception e) {
            log.error("Error running HTTP GET call: ", e);
            throw e;
        }
    }

    private RevolverHttpResponse doPut(RevolverHttpRequest request) throws Exception {
        RevolverHttpApiConfig apiConfiguration = this.getApiConfigurations().get(request.getApi());
        Endpoint endpoint = this.serviceResolver.resolve((this.getServiceConfiguration()).getEndpoint());
        HttpUrl url = generateURI(request, apiConfiguration, endpoint);
        Request.Builder httpRequest = new Request.Builder()
                .url(url);
        httpRequest.put(RequestBody.create(MediaType.parse(request.getHeaders().getFirst("Content-Type")), request.getBody()));
        if (null != request.getHeaders()) {
            request.getHeaders().forEach((key, values) -> values.forEach(value -> httpRequest.addHeader(key, value)));
        }
        trackingHeaders(request, httpRequest);
        Response response;
        try {
            response = client.newCall(httpRequest.build()).execute();
            return this.getHttpResponse(apiConfiguration, response);
        } catch (Exception e) {
            log.error("Error running HTTP GET call: ", e);
            throw e;
        }
    }
    private HttpUrl generateURI(RevolverHttpRequest request, RevolverHttpApiConfig apiConfiguration, Endpoint endpoint) throws URISyntaxException {
        HttpUrl.Builder builder = new HttpUrl.Builder();
        getQueryParams(request, builder);
        if (getServiceConfiguration().isSecured())
            builder.scheme("https");
        else
            builder.scheme("http");
        builder.host(endpoint.getHost()).port(endpoint.getPort()).encodedPath(resolvePath(apiConfiguration, request));
        return builder.build();
    }

    private RevolverHttpResponse getHttpResponse(RevolverHttpApiConfig apiConfiguration, Response response) throws Exception {
        if (!apiConfiguration.getAcceptableResponseCodes().isEmpty() && !apiConfiguration.getAcceptableResponseCodes().contains(response.code())) {
            if (response.body() != null) {
                log.error("Response: " + response.body().string());
            }
            throw new Exception(String.format("HTTP %s %s failed with [%d - %s]", new Object[]{apiConfiguration.getMethod(), apiConfiguration.getApi(), response.code(), response.message()}));
        }
        val headers = new MultivaluedHashMap<String, String>();
        response.headers().names().stream().forEach( h -> {
            headers.putSingle(h, response.header(h));
        });
        return RevolverHttpResponse.builder()
                .statusCode(response.code())
                .headers(headers)
                .body(response.body().bytes())
                .build();
    }

    private String resolvePath(RevolverHttpApiConfig httpApiConfiguration, RevolverHttpRequest request) {
        String uri = null;
        if (Strings.isNullOrEmpty(request.getPath())) {
            if (null != request.getPathParams()) {
                uri = StrSubstitutor.replace(httpApiConfiguration.getPath(), request.getPathParams());
            }
        } else {
            uri = request.getPath();
        }
        if (Strings.isNullOrEmpty(uri)) {
            uri = httpApiConfiguration.getPath();
        }
        return uri.startsWith("/") ? uri : "/" + uri;
    }

    private void getQueryParams(RevolverHttpRequest request, HttpUrl.Builder builder) {
        if (null != request.getQueryParams()) {
            request.getQueryParams().forEach((key, values) -> values.forEach(value -> builder.addQueryParameter(key, value)));
        }
    }

    private void trackingHeaders(RevolverHttpRequest request, Request.Builder requestBuilder) {
        if (!getServiceConfiguration().isTrackingHeaders()) {
            return;
        }
        TraceInfo spanInfo = request.getTrace();
        List<String> existing = request.getHeaders().keySet().stream().map(String::toLowerCase).collect(Collectors.toList());
        if (!existing.contains(TXN_ID_HEADER.toLowerCase())) {
            requestBuilder.addHeader(TXN_ID_HEADER, spanInfo.getTransactionId());
        }
        if (!existing.contains(REQUEST_ID_HEADER.toLowerCase())) {
            requestBuilder.addHeader(REQUEST_ID_HEADER, spanInfo.getRequestId());
        }
        if (!existing.contains(PARENT_REQUEST_ID_HEADER.toLowerCase())) {
            requestBuilder.addHeader(PARENT_REQUEST_ID_HEADER, spanInfo.getParentRequestId());
        }
        if (!existing.contains(TIMESTAMP_HEADER.toLowerCase())) {
            requestBuilder.addHeader(TIMESTAMP_HEADER, Long.toString(spanInfo.getTimestamp()));
        }
        if (!existing.contains(CLIENT_HEADER.toLowerCase())) {
            requestBuilder.addHeader(CLIENT_HEADER, this.getClientConfiguration().getClientName());
        }
    }

}
