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
@Slf4j
@Singleton
@Api(value = "RequestCallback", description = "Revolver gateway api for callbacks on mailbox requests")
public class RevolverCallbackResource {

    private final PersistenceProvider persistenceProvider;

    public RevolverCallbackResource(final PersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    @Path("/v1/callback/{requestId}")
    @POST
    @Metered
    @ApiOperation(value = "Callback for updating responses for a given mailbox request")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
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
