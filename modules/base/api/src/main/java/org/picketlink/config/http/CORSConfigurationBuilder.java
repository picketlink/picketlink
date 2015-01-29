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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.picketlink.http.cors.CORSPathAuthorizer;

/**
 * @author Giriraj Sharma
 */
public class CORSConfigurationBuilder extends AbstractPathConfigurationChildBuilder {

    private boolean allowGenericHttpRequests;
    private Set<String> allowedOrigins;
    private Set<String> supportedMethods;
    private Set<String> supportedHeaders;
    private Set<String> exposedHeaders;
    private boolean supportsCredentials;
    private boolean allowAnyOrigin;
    private boolean supportAnyHeader;
    private long maxAge;
    private List<Class<? extends CORSPathAuthorizer>> corsAuthorizers = new ArrayList<Class<? extends CORSPathAuthorizer>>();

    CORSConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
    }

    public CORSConfigurationBuilder allowedGenericHttpRequests(boolean isGenericHttpRequestsAllowed) {
        this.allowGenericHttpRequests = isGenericHttpRequestsAllowed;
        return this;
    }

    public CORSConfigurationBuilder allowedOrigins(String... allowedOrigins) {
        this.allowedOrigins = new HashSet<String>(Arrays.asList(allowedOrigins));
        return this;
    }

    public CORSConfigurationBuilder supportedMethods(String... supportedMethods) {
        this.supportedMethods = new HashSet<String>(Arrays.asList(supportedMethods));
        return this;
    }

    public CORSConfigurationBuilder supportedHeaders(String... supportedHeaders) {
        this.supportedHeaders = new HashSet<String>(Arrays.asList(supportedHeaders));
        return this;
    }

    public CORSConfigurationBuilder exposedHeaders(String... exposedHeaders) {
        this.exposedHeaders = new HashSet<String>(Arrays.asList(exposedHeaders));
        return this;
    }

    public CORSConfigurationBuilder supportsCredentials(boolean isCredentialsSupported) {
        this.supportsCredentials = isCredentialsSupported;
        return this;
    }

    public CORSConfigurationBuilder allowAnyOrigin(boolean isAnyOriginAllowed) {
        this.allowAnyOrigin = isAnyOriginAllowed;
        return this;
    }

    public CORSConfigurationBuilder allowAnyHeader(boolean isAnyHeaderSupported) {
        this.supportAnyHeader = isAnyHeaderSupported;
        return this;
    }

    public CORSConfigurationBuilder maxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    public CORSConfigurationBuilder authorizer(Class<? extends CORSPathAuthorizer>... pathCORSAuthorizer) {
        this.corsAuthorizers.addAll(Arrays.asList(pathCORSAuthorizer));
        return this;
    }

    CORSConfiguration create(PathConfiguration pathConfiguration) {
        return new CORSConfiguration(pathConfiguration, allowGenericHttpRequests, allowedOrigins, supportedMethods,
                supportedHeaders, exposedHeaders, supportsCredentials, allowAnyOrigin, supportAnyHeader, maxAge,
                corsAuthorizers);
    }
}
