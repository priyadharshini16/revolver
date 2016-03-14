package io.dropwizard.revolver.http.auth;

import lombok.*;

/**
 * @author phaneesh
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BasicAuthConfig extends AuthConfig {

    private String username;
    private String password;
    private String type;
}
