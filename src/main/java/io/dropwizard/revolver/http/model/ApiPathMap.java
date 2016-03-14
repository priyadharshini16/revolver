package io.dropwizard.revolver.http.model;

import lombok.*;

/**
 * @author phaneesh
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ApiPathMap {

    private String api;

    private String path;
}
