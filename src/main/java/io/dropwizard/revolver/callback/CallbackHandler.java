/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.dropwizard.revolver.callback;

import com.google.common.base.Strings;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;
import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import okhttp3.*;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author phaneesh
 */
@Data
@Builder
@Slf4j
public class CallbackHandler {

    private PersistenceProvider persistenceProvider;

    public void handle(final String requestId) {
        final RevolverCallbackRequest request = persistenceProvider.request(requestId);
        if(request == null) {
            log.warn("Invalid request: {}", requestId);
            return;
        }
        RevolverRequestState state = persistenceProvider.requestState(requestId);
        if(state == null) {
            log.warn("Invalid request state: {}", requestId);
            return;
        }
        if(state != RevolverRequestState.RESPONDED) {
            log.warn("Invalid request state {}: {}", state.name(), requestId);
            return;
        }
        if(Strings.isNullOrEmpty(request.getCallbackUri())) {
            log.warn("Invalid callback uri: {}", requestId);
            return;
        }
        try {
            URI uri = new URI(request.getCallbackUri());
            switch (uri.getScheme()) {
                case "https":
                case "http":
                    makeCallback(requestId, uri);
                case "ranger":
                    log.warn("Ranger is not supported yet for request: {}", requestId);
                    break;
                default:
                    log.warn("Invalid protocol for request: {}", requestId);
            }
        } catch (Exception e) {
            log.warn("Invalid callback uri {} for request: {}", request.getCallbackUri(), requestId);
        }
    }

    private void makeCallback(final String requestId, final URI uri) {
        val future = CompletableFuture.supplyAsync(() -> {
            final RevolverCallbackResponse response = persistenceProvider.response(requestId);
            if (response == null) {
                log.warn("Invalid response: {}", requestId);
                return false;
            }
            try {
                OkHttpClient client = CallbackClientManager.getClient(uri.getHost());
                Request.Builder httpRequest = new Request.Builder().url(uri.toURL());
                MediaType mediatype = MediaType.parse("*/*");
                if(response.getHeaders() != null) {
                    response.getHeaders().forEach( (key, headers) ->
                        headers.forEach( value -> httpRequest.addHeader(key, value)));
                    if(response.getHeaders().containsKey(HttpHeaders.CONTENT_TYPE)) {
                        mediatype = MediaType.parse(response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0));
                    }
                }
                if(response.getBody() != null) {
                    httpRequest.post(RequestBody.create(mediatype, response.getBody()));
                } else {
                    httpRequest.post(RequestBody.create(mediatype, new byte[0]));
                }
                Response httpResponse = client.newCall(httpRequest.build()).execute();
                if(!httpResponse.isSuccessful()) {
                    log.error("Error from callback host: {} | Status Code: {} | Response Body: ", uri.getHost(), httpResponse.code(), httpResponse.body() != null ? httpResponse.body().string() : "NONE");
                    return false;
                }
                return true;
            } catch (ExecutionException e) {
                log.error("Cannot get http client for callback host: {}", uri.getHost());
                return false;
            } catch (MalformedURLException e) {
                log.error("Invalid callback URL: {} for request: {}", uri.toString(), requestId);
                return false;
            } catch (IOException e) {
                log.error("Error making callback for: {} for request: {}", uri.toString(), requestId);
                return false;
            }
        });
        future.thenAccept( result -> {
            if(!result) {
                log.error("Error making callback for request: {}", requestId);
            }
        });
    }
}
