package io.dropwizard.revolver.persistence;

import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author phaneesh
 */
@Singleton
public class InMemoryPersistenceProvider implements PersistenceProvider {

    private final ConcurrentHashMap<String, RevolverCallbackRequest> callbackRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RevolverCallbackResponse> callbackResponse = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RevolverRequestState> callbackStates = new ConcurrentHashMap<>();
    private final MultivaluedMap<String, String> mailbox = new MultivaluedHashMap<>();

    @Override
    public void saveRequest(final String requestId, final String mailBoxId, final RevolverCallbackRequest request) {
        callbackRequests.put(requestId, request);
        if(!StringUtils.isBlank(mailBoxId))
            mailbox.add(mailBoxId, requestId);
        callbackStates.put(requestId, RevolverRequestState.RECEIVED);
    }

    @Override
    public void setRequestState(final String requestId, final RevolverRequestState state) {
        callbackStates.put(requestId, state);
    }

    @Override
    public void saveResponse(final String requestId, final RevolverCallbackResponse response) {
        callbackResponse.put(requestId, response);
        callbackStates.put(requestId, RevolverRequestState.RESPONDED);
    }

    @Override
    public RevolverRequestState requestState(final String requestId) {
        return callbackStates.get(requestId);
    }

    @Override
    public RevolverCallbackRequest request(final String requestId) {
        return callbackRequests.get(requestId);
    }

    @Override
    public RevolverCallbackResponse response(final String requestId) {
        return callbackResponse.get(requestId);
    }

}
