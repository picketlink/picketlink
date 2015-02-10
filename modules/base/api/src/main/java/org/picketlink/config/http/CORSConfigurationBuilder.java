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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Giriraj Sharma
 */
public class CORSConfigurationBuilder extends AbstractPathConfigurationChildBuilder {

    private Set<String> allowedOrigins = new HashSet<String>();
    private Set<String> allowedMethods = new HashSet<String>();;
    private Set<String> allowedHeaders = new HashSet<String>();;
    private Set<String> exposedHeaders = new HashSet<String>();;
    private boolean allowCredentials;
    private boolean allowAnyOrigin;
    private boolean allowAnyHeader;
    private long maxAge;
    private boolean allowAnyMethod;

    CORSConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
    }

    public CORSConfigurationBuilder allowOrigins(String... allowedOrigins) {
        this.allowedOrigins.addAll(Arrays.asList(allowedOrigins));
        return this;
    }

    public CORSConfigurationBuilder allowMethods(String... supportedMethods) {
        this.allowedMethods.addAll(Arrays.asList(supportedMethods));
        return this;
    }

    public CORSConfigurationBuilder allowHeaders(String... supportedHeaders) {
        this.allowedHeaders.addAll(Arrays.asList(supportedHeaders));
        return this;
    }

    public CORSConfigurationBuilder exposedHeaders(String... exposedHeaders) {
        this.exposedHeaders.addAll(Arrays.asList(exposedHeaders));
        return this;
    }

    public CORSConfigurationBuilder allowCredentials(boolean isCredentialsSupported) {
        this.allowCredentials = isCredentialsSupported;
        return this;
    }

    public CORSConfigurationBuilder allowAnyOrigin(boolean isAnyOriginAllowed) {
        this.allowAnyOrigin = isAnyOriginAllowed;
        return this;
    }

    public CORSConfigurationBuilder allowAnyHeader(boolean isAnyHeaderSupported) {
        this.allowAnyHeader = isAnyHeaderSupported;
        return this;
    }

    public CORSConfigurationBuilder allowAnyMethod(boolean isAnyMethodSupported) {
        this.allowAnyMethod = isAnyMethodSupported;
        return this;
    }

    public CORSConfigurationBuilder allowAll() {
        allowAnyHeader(true);
        allowAnyMethod(true);
        allowAnyOrigin(true);
        allowCredentials(true);
        return this;
    }

    public CORSConfigurationBuilder maxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    CORSConfiguration create(PathConfiguration pathConfiguration) {
        return new CORSConfiguration(pathConfiguration, allowedOrigins, allowedMethods,
                allowedHeaders, exposedHeaders, allowCredentials, allowAnyOrigin, allowAnyHeader, this.allowAnyMethod, maxAge);
    }
}
