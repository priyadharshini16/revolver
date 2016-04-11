/*
 * Copyright 2016 Phaneesh Nagaraja <phaneesh.n@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.dropwizard.revolver.core.config;

import com.google.common.collect.Lists;
import io.dropwizard.revolver.discovery.ServiceResolverConfig;
import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author phaneesh
 */
@Builder
@AllArgsConstructor
public class RevolverConfig {

    @NotNull
    @Valid
    @Getter
    @Setter
    private ClientConfig clientConfig;

    @NotNull
    @Valid
    @Getter
    @Setter
    private RuntimeConfig global;

    @NotNull
    @Valid
    @Getter
    @Setter
    private ServiceResolverConfig serviceResolverConfig;

    @NotNull
    @NotBlank
    @Getter
    @Setter
    private String hystrixStreamPath;

    @NotNull
    @NotEmpty
    @Valid
    @Getter
    @Setter
    @Singular
    private List<RevolverServiceConfig> services;

    @NotNull
    @Valid
    @Getter
    @Setter
    private MailBoxConfig mailBox;

    public RevolverConfig() {
        this.global = new RuntimeConfig();
        this.serviceResolverConfig = new ServiceResolverConfig();
        this.hystrixStreamPath = "/hystrix.stream";
        this.services = Lists.newArrayList();
    }
}
