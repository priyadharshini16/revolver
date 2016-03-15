package io.dropwizard.revolver.http.model;

import io.dropwizard.revolver.core.model.RevolverResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author phaneesh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class RevolverHttpResponse extends RevolverResponse {

    private int statusCode;

    @Builder
    public RevolverHttpResponse(final MultivaluedMap<String, String> headers, final byte[] body, final int statusCode) {
        super(headers, body);
        this.statusCode = statusCode;
    }
}
