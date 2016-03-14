package io.dropwizard.revolver.http.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author phaneesh
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicAuthConfig extends AuthConfig {

    private String username;
    private String password;
    private String type;
}
