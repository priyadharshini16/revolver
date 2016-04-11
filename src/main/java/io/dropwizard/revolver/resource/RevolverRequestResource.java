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

package io.dropwizard.revolver.resource;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.base.Strings;
import io.dropwizard.jersey.PATCH;
import io.dropwizard.msgpack.MsgPackMediaType;
import io.dropwizard.revolver.RevolverBundle;
import io.dropwizard.revolver.base.core.RevolverAckMessage;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;
import io.dropwizard.revolver.core.tracing.TraceInfo;
import io.dropwizard.revolver.http.RevolverHttpCommand;
import io.dropwizard.revolver.http.RevolversHttpHeaders;
import io.dropwizard.revolver.http.config.RevolverHttpApiConfig;
import io.dropwizard.revolver.http.model.RevolverHttpRequest;
import io.dropwizard.revolver.http.model.RevolverHttpResponse;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author phaneesh
 */
@Path("/apis")
@Slf4j
@Singleton
@Api(value = "Revolver Gateway", description = "Revolver api gateway endpoints")
public class RevolverRequestResource {

    private final ObjectMapper jsonObjectMapper;

    private final ObjectMapper msgPackObjectMapper;

    private final XmlMapper xmlObjectMapper;

    private final PersistenceProvider persistenceProvider;

    public RevolverRequestResource(final ObjectMapper jsonObjectMapper,
                                   final ObjectMapper msgPackObjectMapper,
                                   final XmlMapper xmlObjectMapper,
                                   final PersistenceProvider persistenceProvider) {
        this.jsonObjectMapper = jsonObjectMapper;
        this.msgPackObjectMapper = msgPackObjectMapper;
        this.xmlObjectMapper = xmlObjectMapper;
        this.persistenceProvider = persistenceProvider;
    }

    @GET
    @Path(value="/{service}/{path: .*}")
    @Metered
    @ApiOperation(value = "Revolver GET api endpoint")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response get(@PathParam("service") final String service,
                        @PathParam("path") final String path, @Context final HttpHeaders headers, @Context final UriInfo uriInfo) throws Exception {
        return processRequest(service, RevolverHttpApiConfig.RequestMethod.GET, path, headers, uriInfo, null);
    }

    @HEAD
    @Path(value="/{service}/{path: .*}")
    @Metered
    @ApiOperation(value = "Revolver HEAD api endpoint")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response head(@PathParam("service") final String service,
                        @PathParam("path") final String path, @Context final HttpHeaders headers, @Context final UriInfo uriInfo) throws Exception {
        return processRequest(service, RevolverHttpApiConfig.RequestMethod.HEAD, path, headers, uriInfo, null);
    }

    @POST
    @Path(value="/{service}/{path: .*}")
    @Metered
    @ApiOperation(value = "Revolver POST api endpoint")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response post(@PathParam("service") final String service,
                        @PathParam("path") final String path, @Context final HttpHeaders headers, @Context final UriInfo uriInfo, final byte[] body) throws Exception {
        return processRequest(service, RevolverHttpApiConfig.RequestMethod.POST, path, headers, uriInfo, body);
    }

    @PUT
    @Path(value="/{service}/{path: .*}")
    @Metered
    @ApiOperation(value = "Revolver PUT api endpoint")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response put(@PathParam("service") final String service,
                         @PathParam("path") final String path, @Context final HttpHeaders headers, @Context final UriInfo uriInfo, final byte[] body) throws Exception {
        return processRequest(service, RevolverHttpApiConfig.RequestMethod.PUT, path, headers, uriInfo, body);
    }

    @DELETE
    @Path(value="/{service}/{path: .*}")
    @Metered
    @ApiOperation(value = "Revolver DELETE api endpoint")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response delete(@PathParam("service") final String service,
                        @PathParam("path") final String path, @Context final HttpHeaders headers, @Context final UriInfo uriInfo) throws Exception {
        return processRequest(service, RevolverHttpApiConfig.RequestMethod.DELETE, path, headers, uriInfo, null);
    }

    @PATCH
    @Path(value="/{service}/{path: .*}")
    @Metered
    @ApiOperation(value = "Revolver PATCH api endpoint")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response patch(@PathParam("service") final String service,
                        @PathParam("path") final String path, @Context final HttpHeaders headers, @Context final UriInfo uriInfo, final byte[] body) throws Exception {
        return processRequest(service, RevolverHttpApiConfig.RequestMethod.PATCH, path, headers, uriInfo, body);
    }

    @OPTIONS
    @Path(value="/{service}/{path: .*}")
    @Metered
    @ApiOperation(value = "Revolver OPTIONS api endpoint")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response options(@PathParam("service") final String service,
                          @PathParam("path") final String path, @Context final HttpHeaders headers, @Context final UriInfo uriInfo, final byte[] body) throws Exception {
        return processRequest(service, RevolverHttpApiConfig.RequestMethod.OPTIONS, path, headers, uriInfo, body);
    }


    private Response processRequest(final String service, final RevolverHttpApiConfig.RequestMethod method, final String path,
                                    final HttpHeaders headers, final UriInfo uriInfo, final byte[] body) throws Exception {
        val apiMap = RevolverBundle.matchPath(service, path);
        if(apiMap == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("message", "Bad Request")).build();
        }
        val callMode = headers.getRequestHeaders().getFirst(RevolversHttpHeaders.CALL_MODE_HEADER);
        if(Strings.isNullOrEmpty(callMode)) {
          return executeInline(service, apiMap.getApi().getApi(), method, path, headers, uriInfo, body);
        }
        switch (callMode.toUpperCase()) {
            case RevolverHttpCommand.CALL_MODE_POLLING:
            case RevolverHttpCommand.CALL_MODE_CALLBACK:
                return executeCommandAsync(service, apiMap.getApi().getApi(), method, path, headers, uriInfo, body);
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    private Response executeInline(final String service, final String api, final RevolverHttpApiConfig.RequestMethod method,
                                   final String path, final HttpHeaders headers,
                                   final UriInfo uriInfo, final byte[] body) throws IOException {
        cleanHeaders(headers.getRequestHeaders());
        val httpCommand = RevolverBundle.getHttpCommand(service);
        val response = httpCommand.execute(
                RevolverHttpRequest.builder()
                        .traceInfo(
                                TraceInfo.builder()
                                        .requestId(headers.getHeaderString(RevolversHttpHeaders.REQUEST_ID_HEADER))
                                        .transactionId(headers.getHeaderString(RevolversHttpHeaders.TXN_ID_HEADER))
                                        .timestamp(System.currentTimeMillis())
                                        .build())
                        .api(api)
                        .service(service)
                        .path(path)
                        .method(method)
                        .headers(headers.getRequestHeaders())
                        .queryParams(uriInfo.getQueryParameters())
                        .body(body)
                        .build()
        );
        val httpResponse = Response.status(response.getStatusCode());
        response.getHeaders().keySet().stream().filter( h -> !h.equalsIgnoreCase("Content-Type")).forEach( h -> httpResponse.header(h, response.getHeaders().getFirst(h)));
        val requestMediaType = Strings.isNullOrEmpty(headers.getHeaderString("Accept")) ? "application/json" : headers.getHeaderString("Accept");
        val responseMediaType = Strings.isNullOrEmpty(response.getHeaders().getFirst("Content-Type")) ? "application/json" : response.getHeaders().getFirst("Content-Type");
        if(Strings.isNullOrEmpty(requestMediaType)) {
            httpResponse.header("Content-Type", responseMediaType);
            httpResponse.entity(response.getBody());
        } else {
            if(requestMediaType.startsWith(responseMediaType)) {
                httpResponse.header("Content-Type", responseMediaType);
                httpResponse.entity(response.getBody());
            } else {
                Map<String, Object> responseData = null;
                if(responseMediaType.startsWith(MediaType.APPLICATION_JSON)) {
                    responseData = jsonObjectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>(){});
                } else if(responseMediaType.startsWith(MediaType.APPLICATION_XML)) {
                    responseData = xmlObjectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>(){});
                } else if(responseMediaType.startsWith(MsgPackMediaType.APPLICATION_MSGPACK)) {
                    responseData = msgPackObjectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {});
                }
                if(responseData == null) {
                    httpResponse.entity(response.getBody());
                } else {

                    if(requestMediaType.startsWith(MediaType.APPLICATION_JSON)) {
                        httpResponse.header("Content-Type", MediaType.APPLICATION_JSON);
                        httpResponse.entity(jsonObjectMapper.writeValueAsBytes(responseData));
                    } else if(requestMediaType.startsWith(MediaType.APPLICATION_XML)) {
                        httpResponse.header("Content-Type", MediaType.APPLICATION_XML);
                        httpResponse.entity(xmlObjectMapper.writeValueAsBytes(responseData));
                    } else if(requestMediaType.startsWith(MsgPackMediaType.APPLICATION_MSGPACK)) {
                        httpResponse.header("Content-Type", MsgPackMediaType.APPLICATION_MSGPACK);
                        httpResponse.entity(msgPackObjectMapper.writeValueAsBytes(responseData));
                    } else {
                        httpResponse.header("Content-Type", MediaType.APPLICATION_JSON);
                        httpResponse.entity(jsonObjectMapper.writeValueAsBytes(responseData));
                    }
                }
            }
        }
        return httpResponse.build();
    }

    private void cleanHeaders(final MultivaluedMap<String, String> headers) {
        headers.remove("host");
        headers.remove("Host");
        headers.remove("HOST");
    }

    private Response executeCommandAsync(final String service, final String api, final RevolverHttpApiConfig.RequestMethod method,
                                         final String path, final HttpHeaders headers,
                                         final UriInfo uriInfo, final byte[] body) throws Exception {
        cleanHeaders(headers.getRequestHeaders());
        val httpCommand = RevolverBundle.getHttpCommand(service);
        val requestId = headers.getHeaderString(RevolversHttpHeaders.REQUEST_ID_HEADER);
        val transactionId = headers.getHeaderString(RevolversHttpHeaders.TXN_ID_HEADER);
        val mailBoxId = headers.getHeaderString(RevolversHttpHeaders.MAILBOX_ID_HEADER);
        persistenceProvider.saveRequest(requestId, mailBoxId,
                RevolverCallbackRequest.builder()
                        .api(api)
                        .service(service)
                        .path(path)
                        .headers(headers.getRequestHeaders())
                        .queryParams(uriInfo.getQueryParameters())
                        .body(body)
                        .build()
        );
        CompletableFuture<RevolverHttpResponse> response = httpCommand.executeAsync(
                RevolverHttpRequest.builder()
                        .traceInfo(
                                TraceInfo.builder()
                                        .requestId(requestId)
                                        .transactionId(transactionId)
                                        .timestamp(System.currentTimeMillis())
                                        .build())
                        .api(api)
                        .service(service)
                        .path(path)
                        .method(method)
                        .headers(headers.getRequestHeaders())
                        .queryParams(uriInfo.getQueryParameters())
                        .body(body)
                        .build()
        );
        response.thenAcceptAsync( result -> {
            try {
                persistenceProvider.saveResponse(requestId, RevolverCallbackResponse.builder()
                        .body(result.getBody())
                        .headers(result.getHeaders())
                        .statusCode(result.getStatusCode())
                        .build());
            } catch (Exception e) {
                log.error("Error saving response:", e);
            }
        });
        return Response.accepted().entity(RevolverAckMessage.builder().requestId(requestId).acceptedAt(System.currentTimeMillis()).build()).build();
    }
}
