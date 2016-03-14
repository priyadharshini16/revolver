package io.dropwizard.revolver.resource;

import com.codahale.metrics.annotation.Metered;
import io.dropwizard.msgpack.MsgPackMediaType;
import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.base.core.RevolverRequestStateResponse;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author phaneesh
 */
@Path("/revolver")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MsgPackMediaType.APPLICATION_MSGPACK})
@Slf4j
@Data
@AllArgsConstructor
@Builder
public class RevolverRequestStatusResource {

    private PersistenceProvider persistenceProvider;

    @Path("/v1/request/status/{requestId}")
    @GET
    @Metered
    public Response requestStatus(@PathParam("requestId") final String requestId) {
        try {
            RevolverRequestState state = persistenceProvider.requestState(requestId);
            if(state == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            val response = RevolverRequestStateResponse.builder()
                    .requestId(requestId);
            switch (state) {
                case READ:
                    response.state("COMPLETED");
                    break;
                case RECEIVED:
                    response.state("RECEIVED");
                    break;
                case REQUESTED:
                    response.state("REQUESTED");
                    break;
                case RESPONDED:
                    response.state("RESPONDED");
                    break;
            }
            return Response.ok().entity(response.build()).build();
        } catch (Exception e) {
            log.error("Error getting request state", e);
            return Response.serverError().build();
        }
    }

}
