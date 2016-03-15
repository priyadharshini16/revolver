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
package io.dropwizard.revolver.handler;

import io.dropwizard.revolver.http.RevolverHttpCommand;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.UUID;

/**
 * @author phaneesh
 */
@Slf4j
@Provider
@Priority(Priorities.AUTHENTICATION)
public class RevolverCallbackRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(final ContainerRequestContext containerRequestContext) throws IOException {
        String requestId = containerRequestContext.getHeaderString(RevolverHttpCommand.REQUEST_ID_HEADER);
        val transactionId = containerRequestContext.getHeaderString(RevolverHttpCommand.TXN_ID_HEADER);
        val host = containerRequestContext.getHeaderString("host");
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
