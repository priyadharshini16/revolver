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
import io.dropwizard.msgpack.MsgPackMediaType;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.base.core.RevolverRequestStateResponse;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

/**
 * @author phaneesh
 */
@Path("/revolver")
@Slf4j
@Data
@AllArgsConstructor
@Builder
@Singleton
@Api(value = "MailBox", description = "Revolver gateway api for interacting mailbox requests")
public class RevolverMailboxResource {

    private PersistenceProvider persistenceProvider;

    private static final Map<String, String> notFound = Collections.singletonMap("message", "Request not found");
    private static final Map<String, String> error = Collections.singletonMap("message", "Server error");

    @Path("/v1/request/status/{requestId}")
    @GET
    @Metered
    @ApiOperation(value = "Get the status of the request in the mailbox")
    public Response requestStatus(@PathParam("requestId") final String requestId) {
        try {
            RevolverRequestState state = persistenceProvider.requestState(requestId);
            if(state == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(notFound).build();
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
            return Response.serverError().entity(error).build();
        }
    }

    @Path("/v1/request/status/{requestId}")
    @GET
    @Metered
    @ApiOperation(value = "Get the request in the mailbox")
    public Response request(@PathParam("requestId") final String requestId) {
        try {
            RevolverCallbackRequest callbackRequest = persistenceProvider.request(requestId);
            if(callbackRequest == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(notFound).build();
            }
            return Response.ok().entity(callbackRequest).build();
        } catch (Exception e) {
            log.error("Error getting request state", e);
            return Response.serverError().entity(error).build();
        }
    }

}
