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
package org.picketlink.http.internal.cors;

import org.jboss.logging.Logger;
import org.picketlink.config.http.CORSConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Cross-Origin Resource Sharing (CORS) Class.
 *
 * <p>
 * The class intercepts incoming HTTP requests and applies the CORS policy as specified by the configuration parameters. The
 * actual CORS request is processed by this class.
 *
 * <p>
 * Supported configuration parameters:
 *
 * <ul>
 * <li>cors.allowOrigin {"*"|origin-list} defaults to {@code *}.
 * <li>cors.supportedMethods {method-list} defaults to {@code "GET, POST, HEAD, OPTIONS"}.
 * <li>cors.supportedHeaders {"*"|header-list} defaults to {@code *}.
 * <li>cors.exposedHeaders {header-list} defaults to empty list.
 * <li>cors.supportsCredentials {true|false} defaults to {@code true}.
 * <li>cors.maxAge {int} defaults to {@code -1} (unspecified).
 * </ul>
 *
 * @author Giriraj Sharma
 */
public class CORS {
    protected static final Logger logger = Logger.getLogger(CORS.class);

    public static final String ORIGIN = "Origin";
    public static final String HOST = "Host";

    public static final long DEFAULT_MAX_AGE = TimeUnit.HOURS.toSeconds(1);
    public static final String DEFAULT_ALLOW_METHODS = "GET, POST, HEAD, OPTIONS";
    public static final String DEFAULT_ALLOW_HEADERS = "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers";

    public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD = "*";

    private Set<String> allowedOrigins;
    private Set<String> supportedMethods;
    private Set<String> supportedHeaders;
    private Set<String> exposedHeaders;

    private long maxAge;
    private boolean allowAnyOrigin;
    private boolean supportAnyHeader;
    private boolean supportsCredentials;

    public CORS(CORSConfiguration corsConfiguration) {
        this.allowedOrigins = corsConfiguration.getAllowedOrigins();
        this.supportedMethods = corsConfiguration.getSupportedMethods();
        this.supportedHeaders = corsConfiguration.getSupportedHeaders();
        this.exposedHeaders = corsConfiguration.getExposedHeaders();
        this.maxAge = corsConfiguration.getMaxAge();
        this.supportsCredentials = corsConfiguration.isCredentialsSupported();
        this.allowAnyOrigin = corsConfiguration.isAnyOriginAllowed();
        this.supportAnyHeader = corsConfiguration.isAnyHeaderSupported();

    }

    public void handleActualRequest(CORSConfiguration corsConfiguration, HttpServletRequest request,
            HttpServletResponse response) {

        final String requestOrigin = request.getHeader(ORIGIN);

        if (requestOrigin == null) {
            logger.debug("CORS origin header is null");
            throw new RuntimeException("CORS origin header is null");
        }

        if (allowedOrigins == null
                || (!allowedOrigins.contains(requestOrigin) && !allowedOrigins.contains(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD) && !allowAnyOrigin)) {
            logger.debug("CORS origin denied " + requestOrigin);
            throw new RuntimeException("CORS origin denied " + requestOrigin);
        }

        final String method = request.getMethod().toUpperCase();
        if (!supportedMethods.contains(method)) {
            logger.debug("Unsupported HTTP method " + method);
            throw new RuntimeException("Unsupported HTTP method " + method);
        }

        if (supportsCredentials) {
            response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
        } else {
            if (allowAnyOrigin) {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD);
            } else {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
            }
        }

        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            response.addHeader(ACCESS_CONTROL_EXPOSE_HEADERS, CorsUtil.join(exposedHeaders));
        }

    }

    public void handlePreflightRequest(CORSConfiguration corsConfiguration, HttpServletRequest request,
            HttpServletResponse response) {

        final String requestOrigin = request.getHeader(ORIGIN);

        if (requestOrigin == null) {
            logger.debug("CORS origin header is null");
            throw new RuntimeException("CORS origin header is null");
        }

        if (allowedOrigins == null
                || (!allowedOrigins.contains(requestOrigin) && !allowedOrigins.contains(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD) && !allowAnyOrigin)) {
            logger.debug("CORS origin denied " + requestOrigin);
            throw new RuntimeException("CORS origin denied " + requestOrigin);
        }

        final String requestMethodHeader = request.getHeader(ACCESS_CONTROL_REQUEST_METHOD);
        if (requestMethodHeader == null) {
            logger.debug("Invalid preflight CORS request: Missing Access-Control-Request-Method header");
            throw new RuntimeException("Invalid preflight CORS request: Missing Access-Control-Request-Method header");
        }

        String requestedMethod = requestMethodHeader.toUpperCase();

        // Parse the requested author (custom) headers
        final String rawRequestHeadersString = request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS);
        final String[] requestHeaderValues = CorsUtil.parseMultipleHeaderValues(rawRequestHeadersString);
        final String[] requestHeaders = new String[requestHeaderValues.length];

        for (int i = 0; i < requestHeaders.length; i++) {
            try {
                requestHeaders[i] = CorsUtil.formatCanonical(requestHeaderValues[i]);
            } catch (IllegalArgumentException e) {
                // Invalid header name
                logger.debug("Invalid preflight CORS request: Bad request header value " + requestHeaderValues[i]);
                throw new RuntimeException("Invalid preflight CORS request: Bad request header value " + requestHeaderValues[i]);
            }
        }

        if (!supportedMethods.contains(requestedMethod)) {
            logger.debug("Unsupported HTTP access control request method " + requestedMethod);
            throw new RuntimeException("Unsupported HTTP access control request method " + requestedMethod);
        }

        // Author request headers check
        if (!supportAnyHeader) {
            for (String requestHeader : requestHeaders) {
                if (!supportedHeaders.contains(requestHeader)) {
                    logger.debug("Unsupported HTTP access control request header " + requestHeader);
                    throw new RuntimeException("Unsupported HTTP access control request header " + requestHeader);
                }
            }
        }

        if (supportsCredentials) {
            response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
        } else {
            if (allowAnyOrigin) {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD);
            } else {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
            }
        }

        if (Long.valueOf(maxAge) != null && maxAge > 0) {
            response.addHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(maxAge));
        } else {
            response.addHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(DEFAULT_MAX_AGE));
        }

        if (supportedMethods != null) {
            response.addHeader(ACCESS_CONTROL_ALLOW_METHODS, CorsUtil.join(supportedMethods));
        } else {
            response.addHeader(ACCESS_CONTROL_ALLOW_METHODS, DEFAULT_ALLOW_METHODS);
        }

        if (supportAnyHeader && rawRequestHeadersString != null) {
            response.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, rawRequestHeadersString);
        } else if (supportedHeaders != null && !supportedHeaders.isEmpty()) {
            response.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, CorsUtil.join(supportedHeaders));
        }

    }

}
