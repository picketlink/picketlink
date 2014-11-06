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
package org.picketlink.config.http;

import static java.util.Collections.unmodifiableList;

import java.util.List;

import org.picketlink.http.cors.PathCORSAuthorizer;

/**
 * @author Giriraj Sharma
 */
public class CORSConfiguration {

    private final boolean allowGenericHttpRequests;
    private final String[] allowedOrigins;
    private final boolean allowSubdomains;
    private final String[] supportedMethods;
    private final String[] supportedHeaders;
    private final String[] exposedHeaders;
    private final boolean supportsCredentials;
    private final int maxAge;
    private final List<Class<? extends PathCORSAuthorizer>> corsAuthorizers;
    private final PathConfiguration pathConfiguration;

    public CORSConfiguration(PathConfiguration pathConfiguration, boolean allowGenericHttpRequests, String[] allowedOrigins,
            boolean allowSubdomains, String[] supportedMethods, String[] supportedHeaders, String[] exposedHeaders,
            boolean supportsCredentials, int maxAge, List<Class<? extends PathCORSAuthorizer>> authorizers) {

        this.pathConfiguration = pathConfiguration;
        this.allowGenericHttpRequests = allowGenericHttpRequests;
        this.allowedOrigins = allowedOrigins;
        this.allowSubdomains = allowSubdomains;
        this.supportedMethods = supportedMethods;
        this.supportedHeaders = supportedHeaders;
        this.exposedHeaders = exposedHeaders;
        this.supportsCredentials = supportsCredentials;
        this.maxAge = maxAge;
        this.corsAuthorizers = authorizers;
    }

    public boolean isGenericHttpRequestsAllowed() {
        return this.allowGenericHttpRequests;
    }

    public String[] getAllowedOrigins() {
        return this.allowedOrigins;
    }

    public boolean isSubdomainsAllowed() {
        return this.allowSubdomains;
    }

    public String[] getSupportedMethods() {
        return this.supportedMethods;
    }

    public String[] getSupportedHeaders() {
        return this.supportedHeaders;
    }

    public String[] getExposedHeaders() {
        return this.exposedHeaders;
    }

    public boolean isCredentialsSupported() {
        return this.supportsCredentials;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public List<Class<? extends PathCORSAuthorizer>> getAuthorizers() {
        return unmodifiableList(this.corsAuthorizers);
    }
}
