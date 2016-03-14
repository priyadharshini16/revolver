package io.dropwizard.revolver.http.auth;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author phaneesh
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
public abstract class AuthConfig {

    public abstract String getType();

}
