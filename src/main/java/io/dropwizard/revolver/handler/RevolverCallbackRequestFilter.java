/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dropwizard.revolver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.io.ByteStreams;
import io.dropwizard.revolver.RevolverBundle;
import io.dropwizard.revolver.base.core.RevolverAckMessage;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;
import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.core.tracing.TraceInfo;
import io.dropwizard.revolver.http.RevolverHttpCommand;
import io.dropwizard.revolver.http.model.RevolverHttpRequest;
import io.dropwizard.revolver.http.model.RevolverHttpResponse;
import io.dropwizard.revolver.persistence.PersistenceProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author phaneesh
 */
@Slf4j
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RevolverCallbackRequestFilter implements ContainerRequestFilter {

    private PersistenceProvider persistenceProvider;

    private ObjectMapper objectMapper;

    public RevolverCallbackRequestFilter(final PersistenceProvider persistenceProvider, final ObjectMapper objectMapper) {
        this.persistenceProvider = persistenceProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String requestId = containerRequestContext.getHeaderString(RevolverHttpCommand.REQUEST_ID_HEADER);
        String transactionId = containerRequestContext.getHeaderString(RevolverHttpCommand.TXN_ID_HEADER);
        String host = containerRequestContext.getHeaderString("host");
        containerRequestContext.getHeaders().putSingle("X-FORWARDED-FOR", host);
        if(StringUtils.isBlank(requestId)) {
            requestId = UUID.randomUUID().toString();
            containerRequestContext.getHeaders().putSingle(RevolverHttpCommand.REQUEST_ID_HEADER, requestId);
        }
        if(StringUtils.isBlank(transactionId)) {
            containerRequestContext.getHeaders().putSingle(RevolverHttpCommand.TXN_ID_HEADER, requestId);
        }
    }




}
