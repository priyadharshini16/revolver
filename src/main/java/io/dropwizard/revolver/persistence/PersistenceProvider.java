package io.dropwizard.revolver.persistence;

import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;

/**
 * @author phaneesh
 */
public interface PersistenceProvider {

    void saveRequest(final String requestId, final String mailboxId, final RevolverCallbackRequest request) throws Exception;

    void setRequestState(final String requestId, RevolverRequestState state) throws Exception;

    void saveResponse(final String requestId, RevolverCallbackResponse response) throws Exception;

    RevolverRequestState requestState(final String requestId) throws Exception;

    RevolverCallbackResponse response(final String requestId) throws Exception;

    RevolverCallbackRequest request(final String requestId) throws Exception;

}
