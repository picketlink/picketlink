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

    /**
     * <p>Use this method to specify the origins allowed to access a specific path.</p>
     *
     * @param allowedOrigins A single string or an array of allowed origins.
     * @return
     */
    public CORSConfigurationBuilder allowOrigins(String... allowedOrigins) {
        this.allowedOrigins.addAll(Arrays.asList(allowedOrigins));
        return this;
    }

    /**
     * <p>Use this method to specify the HTTP methods allowed to access a specific path.</p>
     *
     * @param allowedMethods A single string or an array of HTTP methods.
     * @return
     */
    public CORSConfigurationBuilder allowMethods(String... allowedMethods) {
        this.allowedMethods.addAll(Arrays.asList(allowedMethods));
        return this;
    }

    /**
     * <p>Use this method to specify the HTTP headers allowed to access a specific path.</p>
     *
     * @param allowedHeaders A single string or an array of HTTP headers.
     * @return
     */
    public CORSConfigurationBuilder allowHeaders(String... allowedHeaders) {
        this.allowedHeaders.addAll(Arrays.asList(allowedHeaders));
        return this;
    }

    /**
     * <p>Use this method to specify which HTTP headers must be exposed to clients (eg.: XMLHttpRequest)
     * in addition to the default ones, as defined by the specification.</p>
     *
     * @param exposedHeaders A single string or an array of HTTP headers.
     * @return
     */
    public CORSConfigurationBuilder exposedHeaders(String... exposedHeaders) {
        this.exposedHeaders.addAll(Arrays.asList(exposedHeaders));
        return this;
    }

    /**
     * <p>Use this method to indicate that the actual request can include user credentials.</p>
     *
     * @param allowCredentials True if credentials are allowed. Otherwise, set to false. Defaults to false.
     * @return
     */
    public CORSConfigurationBuilder allowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    /**
     * <p>Use this method to indicate that any origin is allowed to access a specific path.</p>
     *
     * @param allowAnyOrigin Defaults to false.
     * @return
     */
    public CORSConfigurationBuilder allowAnyOrigin(boolean allowAnyOrigin) {
        this.allowAnyOrigin = allowAnyOrigin;
        return this;
    }

    /**
     * <p>Use this method to indicate that any HTTP header is allowed for a specific path.</p>
     *
     * @param allowAnyHeader Defaults to false.
     * @return
     */
    public CORSConfigurationBuilder allowAnyHeader(boolean allowAnyHeader) {
        this.allowAnyHeader = allowAnyHeader;
        return this;
    }

    /**
     * <p>Use this method to indicate that any HTTP method is allowed for a specific path.</p>
     *
     * @param allowAnyMethod Defaults to false.
     * @return
     */
    public CORSConfigurationBuilder allowAnyMethod(boolean allowAnyMethod) {
        this.allowAnyMethod = allowAnyMethod;
        return this;
    }

    /**
     * <p>Convenience method to still use CORS but disable validations by allowing everything.</p>
     *
     * @return
     */
    public CORSConfigurationBuilder allowAll() {
        allowAnyHeader(true);
        allowAnyMethod(true);
        allowAnyOrigin(true);
        allowCredentials(true);
        return this;
    }

    /**
     * <p>Use this method to indicate indicates how long the results of a preflight request can be cached.</p>
     *
     * @param maxAge An interget number of seconds.
     * @return
     */
    public CORSConfigurationBuilder maxAge(long maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    CORSConfiguration create(PathConfiguration pathConfiguration) {
        return new CORSConfiguration(pathConfiguration, allowedOrigins, allowedMethods,
                allowedHeaders, exposedHeaders, allowCredentials, allowAnyOrigin, allowAnyHeader, this.allowAnyMethod, maxAge);
    }
}
