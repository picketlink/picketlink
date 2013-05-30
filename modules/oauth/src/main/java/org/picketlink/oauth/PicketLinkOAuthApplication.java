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
package org.picketlink.oauth;

import org.picketlink.oauth.registration.RegistrationEndpoint;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;
import org.picketlink.oauth.server.endpoint.ResourceEndpoint;
import org.picketlink.oauth.server.endpoint.TokenEndpoint;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * A JAXRS Application for OAuth
 *
 * @author anil saldhana
 * @since Sep 23, 2012
 */
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class PicketLinkOAuthApplication extends Application {
    private static Set services = new HashSet();

    public PicketLinkOAuthApplication() {
        // initialize restful services
        services.add(new RegistrationEndpoint());
        services.add(new AuthorizationEndpoint());
        services.add(new TokenEndpoint());
        services.add(new ResourceEndpoint());
    }

    @Override
    public Set getSingletons() {
        return services;
    }

    public static Set getServices() {
        return services;
    }

}