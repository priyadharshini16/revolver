package io.dropwizard.revolver.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jvnet.hk2.component.MultiMap;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "responseBuilder")
public class RevolverResponse {

    private MultivaluedMap<String, String> headers;

    private byte[] body;
}
