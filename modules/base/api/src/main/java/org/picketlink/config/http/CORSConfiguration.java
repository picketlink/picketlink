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

import java.util.Set;

/**
 * @author Giriraj Sharma
 */
public class CORSConfiguration {

    private final Set<String> allowedOrigins;
    private final Set<String> allowedMethods;
    private final Set<String> allowedHeaders;
    private final Set<String> exposedHeaders;
    private final boolean allowCredentials;
    private final boolean allowAnyOrigin;
    private final boolean allowAnyHeader;
    private final long maxAge;
    private final PathConfiguration pathConfiguration;
    private final boolean allowAnyMethod;

    public CORSConfiguration(PathConfiguration pathConfiguration, Set<String> allowedOrigins,
                             Set<String> allowedMethods, Set<String> allowedHeaders, Set<String> exposedHeaders,
                             boolean allowCredentials, boolean allowAnyOrigin, boolean allowAnyHeader, boolean allowAnyMethod, long maxAge) {

        this.pathConfiguration = pathConfiguration;
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.exposedHeaders = exposedHeaders;
        this.allowCredentials = allowCredentials;
        this.allowAnyOrigin = allowAnyOrigin;
        this.allowAnyHeader = allowAnyHeader;
        this.allowAnyMethod = allowAnyMethod;
        this.maxAge = maxAge;
    }

    public Set<String> getAllowedOrigins() {
        return this.allowedOrigins;
    }

    public Set<String> getAllowedMethods() {
        return this.allowedMethods;
    }

    public Set<String> getAllowedHeaders() {
        return this.allowedHeaders;
    }

    public Set<String> getExposedHeaders() {
        return this.exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return this.allowCredentials;
    }

    public boolean isAllowAnyOrigin() {
        return this.allowAnyOrigin;
    }

    public boolean isAllowAnyHeader() {
        return this.allowAnyHeader;
    }

    public boolean isAllowAnyMethod() {
        return this.allowAnyMethod;
    }

    public long getMaxAge() {
        return this.maxAge;
    }

}
