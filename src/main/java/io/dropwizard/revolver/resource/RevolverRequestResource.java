package io.dropwizard.revolver.resource;

import com.codahale.metrics.annotation.Metered;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.dropwizard.jersey.PATCH;
import io.dropwizard.msgpack.MsgPackMediaType;
import io.dropwizard.revolver.RevolverBundle;
import io.dropwizard.revolver.core.tracing.TraceInfo;
import io.dropwizard.revolver.http.RevolverHttpCommand;
import io.dropwizard.revolver.http.model.RevolverHttpRequest;
import io.dropwizard.revolver.http.model.RevolverHttpResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author phaneesh
 */
@Path("/")
@Slf4j
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK})
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK})
public class RevolverRequestResource {

    private final ObjectMapper jsonObjectMapper;

    private final ObjectMapper msgPackObjectMapper;

    private final XmlMapper xmlObjectMapper;

    public RevolverRequestResource(final ObjectMapper jsonObjectMapper, final ObjectMapper msgPackObjectMapper, final XmlMapper xmlObjectMapper) {
        this.jsonObjectMapper = jsonObjectMapper;
        this.msgPackObjectMapper = msgPackObjectMapper;
        this.xmlObjectMapper = xmlObjectMapper;
    }

    @GET
    @Path(value="/{service}/{api}/{path: .*}")
    @Metered
    public Response get(@PathParam("service") final String service, @PathParam("api") final String api,
                        @PathParam("path") final String path, @Context HttpHeaders headers, @Context UriInfo uriInfo) throws IOException {
        return processRequest(service, api, path, headers, uriInfo, null);
    }

    @HEAD
    @Path(value="/{service}/{api}/{path: .*}")
    @Metered
    public Response head(@PathParam("service") final String service, @PathParam("api") final String api,
                        @PathParam("path") final String path, @Context HttpHeaders headers, @Context UriInfo uriInfo) throws IOException {
        return processRequest(service, api, path, headers, uriInfo, null);
    }

    @POST
    @Path(value="/{service}/{api}/{path: .*}")
    @Metered
    public Response post(@PathParam("service") final String service, @PathParam("api") final String api,
                        @PathParam("path") final String path, @Context HttpHeaders headers, @Context UriInfo uriInfo, byte[] body) throws IOException {
        return processRequest(service, api, path, headers, uriInfo, body);
    }

    @PUT
    @Path(value="/{service}/{api}/{path: .*}")
    @Metered
    public Response put(@PathParam("service") final String service, @PathParam("api") final String api,
                         @PathParam("path") final String path, @Context HttpHeaders headers, @Context UriInfo uriInfo, byte[] body) throws IOException {
        return processRequest(service, api, path, headers, uriInfo, body);
    }

    @DELETE
    @Path(value="/{service}/{api}/{path: .*}")
    @Metered
    public Response delete(@PathParam("service") final String service, @PathParam("api") final String api,
                        @PathParam("path") final String path, @Context HttpHeaders headers, @Context UriInfo uriInfo) throws IOException {
        return processRequest(service, api, path, headers, uriInfo, null);
    }

    @PATCH
    @Path(value="/{service}/{api}/{path: .*}")
    @Metered
    public Response patch(@PathParam("service") final String service, @PathParam("api") final String api,
                        @PathParam("path") final String path, @Context HttpHeaders headers, @Context UriInfo uriInfo, byte[] body) throws IOException {
        return processRequest(service, api, path, headers, uriInfo, body);
    }


    private Response processRequest(final String service,final String api, final String path, HttpHeaders headers,
                                                UriInfo uriInfo, byte[] body) throws IOException {
        cleanHeaders(headers.getRequestHeaders());
        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand(service);
        RevolverHttpResponse response = httpCommand.execute(
                RevolverHttpRequest.builder()
                        .traceInfo(
                                TraceInfo.builder()
                                        .requestId(UUID.randomUUID().toString())
                                        .transactionId(UUID.randomUUID().toString())
                                        .timestamp(System.currentTimeMillis())
                                        .build())
                        .api(api)
                        .service(service)
                        .path(path)
                        .headers(headers.getRequestHeaders())
                        .queryParams(uriInfo.getQueryParameters())
                        .body(body)
                        .build()
        );


        val httpResponse = Response.status(response.getStatusCode());
        response.getHeaders().keySet().stream().filter( h -> !h.equalsIgnoreCase("Content-Type")).forEach( h -> httpResponse.header(h, response.getHeaders().getFirst(h)));

        val requestMediaType = headers.getHeaderString("Accept");
        val responseMediaType = response.getHeaders().getFirst("Content-Type");
        if(StringUtils.isBlank(requestMediaType)) {
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

    private void cleanHeaders(MultivaluedMap<String, String> headers) {
        headers.remove("host");
        headers.remove("Host");
        headers.remove("HOST");
    }

//    private void executeCommandAsync(final String requestId, final String transactionId, RevolverCallbackRequest callbackRequest) throws Exception {
//        RevolverHttpCommand httpCommand = RevolverBundle.getHttpCommand(callbackRequest.getService());
//        CompletableFuture<RevolverHttpResponse> response = httpCommand.executeAsync(
//                RevolverHttpRequest.builder()
//                        .traceInfo(
//                                TraceInfo.builder()
//                                        .requestId(requestId)
//                                        .transactionId(transactionId)
//                                        .timestamp(System.currentTimeMillis())
//                                        .build())
//                        .api(callbackRequest.getApi())
//                        .service(callbackRequest.getService())
//                        .path(callbackRequest.getPath())
//                        .headers(callbackRequest.getHeaders())
//                        .queryParams(callbackRequest.getQueryParams())
//                        .build()
//        );
//        response.thenAccept( result -> {
//            try {
//                persistenceProvider.saveResponse(requestId, RevolverCallbackResponse.builder()
//                        .body(result.getBody())
//                        .headers(result.getHeaders())
//                        .statusCode(result.getStatusCode())
//                        .build());
//                persistenceProvider.setRequestState(requestId, RevolverRequestState.RESPONDED);
//            } catch (Exception e) {
//                log.error("Error saving response:", e);
//            }
//        });
//    }
//
//    private void sendError(ContainerRequestContext containerRequestContext, Response.Status status) throws IOException {
//        containerRequestContext.abortWith(
//                Response.status(status).build()
//        );
//    }
//
//    private void sendAccepted(final String requestId, ContainerRequestContext containerRequestContext) {
//        containerRequestContext.abortWith(
//                Response.accepted().entity(
//                        RevolverAckMessage.builder()
//                                .acceptedAt(System.currentTimeMillis())
//                                .requestId(requestId)
//                                .build()
//                ).build()
//        );
//    }
}
