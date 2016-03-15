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
