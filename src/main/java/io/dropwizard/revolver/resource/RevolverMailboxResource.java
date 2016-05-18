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
import com.google.common.base.Strings;
import io.dropwizard.msgpack.MsgPackMediaType;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;
import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.base.core.RevolverRequestStateResponse;
import io.dropwizard.revolver.exception.RevolverException;
import io.dropwizard.revolver.http.RevolversHttpHeaders;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
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
    public RevolverRequestStateResponse requestStatus(@PathParam("requestId") final String requestId) throws RevolverException {
        try {
            RevolverRequestState state = persistenceProvider.requestState(requestId);
            if(state == null) {
                throw RevolverException.builder()
                        .status(Response.Status.NOT_FOUND.getStatusCode())
                        .message("Not found")
                        .errorCode("R002")
                        .build();
            }
            return RevolverRequestStateResponse.builder()
                    .requestId(requestId)
                    .state(state.name())
                    .build();
        } catch (Exception e) {
            log.error("Error getting request state", e);
            throw RevolverException.builder()
                    .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .errorCode("R001")
            .message(ExceptionUtils.getRootCause(e).getMessage()).build();
        }
    }

    @Path("/v1/request/ack/{requestId}")
    @POST
    @Metered
    @ApiOperation(value = "Send ack for a request so that the mailbox message can be marked as read")
    public Response ack(@PathParam("requestId") final String requestId) throws RevolverException {
        try {
            RevolverRequestState state = persistenceProvider.requestState(requestId);
            if(state == null) {
                throw RevolverException.builder()
                        .status(Response.Status.NOT_FOUND.getStatusCode())
                        .message("Not found")
                        .errorCode("R002")
                        .build();
            }
            switch (state) {
                case RESPONDED:
                case ERROR:
                    persistenceProvider.setRequestState(requestId, RevolverRequestState.READ);
                    return Response.accepted().build();
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("Error getting request state", e);
            throw RevolverException.builder()
                    .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .errorCode("R001")
                    .message(ExceptionUtils.getRootCause(e).getMessage()).build();
        }
    }

    @Path("/v1/request/{requestId}")
    @GET
    @Metered
    @ApiOperation(value = "Get the request in the mailbox")
    public RevolverCallbackRequest request(@PathParam("requestId") final String requestId) throws RevolverException {
        try {
            RevolverCallbackRequest callbackRequest = persistenceProvider.request(requestId);
            if(callbackRequest == null) {
                throw RevolverException.builder()
                        .status(Response.Status.NOT_FOUND.getStatusCode())
                        .message("Not found")
                        .errorCode("R002")
                        .build();
            }
            return callbackRequest;
        } catch (Exception e) {
            log.error("Error getting request", e);
            throw RevolverException.builder()
                    .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .errorCode("R001")
                    .message(ExceptionUtils.getRootCause(e).getMessage()).build();

        }
    }

    @Path("/v1/response/{requestId}")
    @GET
    @Metered
    @ApiOperation(value = "Get the response for a request in the mailbox")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public Response response(@PathParam("requestId") final String requestId) throws RevolverException {
        try {
            RevolverCallbackResponse callbackResponse = persistenceProvider.response(requestId);
            if(callbackResponse == null) {
                throw RevolverException.builder()
                        .status(Response.Status.NOT_FOUND.getStatusCode())
                        .message("Not found")
                        .errorCode("R002")
                        .build();
            }
            val response = Response.status(callbackResponse.getStatusCode())
                    .entity(callbackResponse.getBody());
            callbackResponse.getHeaders().forEach(response::header);
            return response.build();
        } catch (Exception e) {
            log.error("Error getting response", e);
            throw RevolverException.builder()
                    .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .errorCode("R001")
                    .message(ExceptionUtils.getRootCause(e).getMessage()).build();
        }
    }

    @Path("/v1/requests")
    @GET
    @Metered
    @ApiOperation(value = "Get all the requests in the mailbox")
    public List<RevolverCallbackRequest> requests(@HeaderParam(RevolversHttpHeaders.MAILBOX_ID_HEADER) final String mailboxId) throws RevolverException {
        try {
            List<RevolverCallbackRequest> callbackRequests = persistenceProvider.requests(mailboxId);
            if(callbackRequests == null) {
                throw RevolverException.builder()
                        .status(Response.Status.NOT_FOUND.getStatusCode())
                        .message("Not found")
                        .errorCode("R002")
                        .build();
            }
            return callbackRequests;
        } catch (Exception e) {
            log.error("Error getting requests", e);
            throw RevolverException.builder()
                    .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .errorCode("R001")
                    .message(ExceptionUtils.getRootCause(e).getMessage()).build();

        }
    }

    @Path("/v1/responses")
    @GET
    @Metered
    @ApiOperation(value = "Get all the responses in the mailbox")
    @Produces({MediaType.APPLICATION_JSON, MsgPackMediaType.APPLICATION_MSGPACK, MediaType.APPLICATION_XML})
    public List<RevolverCallbackResponse> responses(@HeaderParam(RevolversHttpHeaders.MAILBOX_ID_HEADER) final String mailboxId) throws RevolverException {
        try {
            if(Strings.isNullOrEmpty(mailboxId)) {
                throw RevolverException.builder()
                        .status(Response.Status.BAD_REQUEST.getStatusCode())
                        .message("Invalid Mailbox Id")
                        .errorCode("R003")
                        .build();
            }
            List<RevolverCallbackResponse> callbackResponses = persistenceProvider.responses(mailboxId);
            if(callbackResponses == null) {
                throw RevolverException.builder()
                        .status(Response.Status.NOT_FOUND.getStatusCode())
                        .message("Not found")
                        .errorCode("R002")
                        .build();
            }
            return callbackResponses;
        } catch (Exception e) {
            log.error("Error getting responses", e);
            throw RevolverException.builder()
                    .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .errorCode("R001")
                    .message(ExceptionUtils.getRootCause(e).getMessage()).build();

        }
    }
}
