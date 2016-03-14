package io.dropwizard.revolver.persistence;

import io.dropwizard.revolver.base.core.RevolverRequestState;
import io.dropwizard.revolver.base.core.RevolverCallbackRequest;
import io.dropwizard.revolver.base.core.RevolverCallbackResponse;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author phaneesh
 */
public class InMemoryPersistenceProvider implements PersistenceProvider {

    private final ConcurrentHashMap<String, RevolverCallbackRequest> callbackRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RevolverCallbackResponse> callbackResponse = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RevolverRequestState> callbackStates = new ConcurrentHashMap<>();

    @Override
    public void saveRequest(String requestId, RevolverCallbackRequest request) throws Exception {
        callbackRequests.put(requestId, request);
    }

    @Override
    public void setRequestState(String requestId, RevolverRequestState state) throws Exception {
        callbackStates.put(requestId, state);
    }

    @Override
    public void saveResponse(String requestId, RevolverCallbackResponse response) throws Exception {
        callbackResponse.put(requestId, response);
    }

    @Override
    public RevolverRequestState requestState(String requestId) throws Exception {
        return callbackStates.get(requestId);
    }

    @Override
    public RevolverCallbackRequest request(String requestId) throws Exception {
        return callbackRequests.get(requestId);
    }

    @Override
    public RevolverCallbackResponse response(String requestId) throws Exception {
        return callbackResponse.get(requestId);
    }

}
