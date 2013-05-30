/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.scim;

import org.picketlink.scim.endpoints.GroupsEndpoint;
import org.picketlink.scim.endpoints.UsersEndpoint;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application
 *
 * @author anil saldhana
 * @since Apr 9, 2013
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class PicketLinkSCIMApplication extends Application {
    private static Set services = new HashSet();

    public PicketLinkSCIMApplication() {
        // initialize restful services
        services.add(new UsersEndpoint());
        services.add(new GroupsEndpoint());
    }

    @Override
    public Set getSingletons() {
        return services;
    }

    public static Set getServices() {
        return services;
    }
}