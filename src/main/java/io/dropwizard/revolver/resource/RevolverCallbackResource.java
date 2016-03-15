package io.dropwizard.revolver.resource;

import com.codahale.metrics.annotation.Metered;
import com.google.common.io.ByteStreams;
import io.dropwizard.msgpack.MsgPackMediaType;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;
import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author phaneesh
 */
@Path("/revolver")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MsgPackMediaType.APPLICATION_MSGPACK})
@Slf4j
@Singleton
@Api(value = "RequestCallback", description = "Revolver gateway api for callbacks on mailbox requests")
public class RevolverCallbackResource {

    public final PersistenceProvider persistenceProvider;

    public RevolverCallbackResource(final PersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    @Path("/v1/callback/{requestId}")
    @POST
    @Metered
    @ApiOperation(value = "Callback for updating responses for a given mailbox request")
    public Response handleCallback(@PathParam("requestId") final String requestId,
                                   @HeaderParam("X-RESPONSE-CODE") final String responseCode,
                                   @Context final HttpHeaders headers,
                                   @Context final HttpServletRequest request) {
        try {
            val response = RevolverCallbackResponse.builder()
                    .body(ByteStreams.toByteArray(request.getInputStream()))
                    .headers(headers.getRequestHeaders())
                    .statusCode(responseCode != null ? Integer.parseInt(responseCode) : 200)
                    .build();
            persistenceProvider.saveResponse(requestId, response);
            persistenceProvider.setRequestState(requestId, RevolverRequestState.RESPONDED);
            return Response.accepted().build();
        } catch(Exception e) {
            log.error("Callback error", e);
            return Response.serverError().build();
        }
    }
}
