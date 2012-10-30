/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.oauth;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.picketlink.oauth.registration.RegistrationEndpoint;
import org.picketlink.oauth.server.endpoint.AuthorizationEndpoint;
import org.picketlink.oauth.server.endpoint.ResourceEndpoint;
import org.picketlink.oauth.server.endpoint.TokenEndpoint;

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