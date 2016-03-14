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
    public void saveRequest(String requestId, String mailBoxId, RevolverCallbackRequest request) throws Exception {
        callbackRequests.put(requestId, request);
        if(!StringUtils.isBlank(mailBoxId))
            mailbox.add(mailBoxId, requestId);
        callbackStates.put(requestId, RevolverRequestState.RECEIVED);
    }

    @Override
    public void setRequestState(String requestId, RevolverRequestState state) throws Exception {
        callbackStates.put(requestId, state);
    }

    @Override
    public void saveResponse(String requestId, RevolverCallbackResponse response) throws Exception {
        callbackResponse.put(requestId, response);
        callbackStates.put(requestId, RevolverRequestState.RESPONDED);
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
