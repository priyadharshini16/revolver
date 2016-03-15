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

package io.dropwizard.revolver.persistence;

import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;

/**
 * @author phaneesh
 */
public interface PersistenceProvider {

    void saveRequest(final String requestId, final String mailboxId, final RevolverCallbackRequest request);

    void setRequestState(final String requestId, RevolverRequestState state);

    void saveResponse(final String requestId, RevolverCallbackResponse response);

    RevolverRequestState requestState(final String requestId);

    RevolverCallbackResponse response(final String requestId);

    RevolverCallbackRequest request(final String requestId);

}
