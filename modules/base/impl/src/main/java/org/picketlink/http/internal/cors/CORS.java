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

import org.picketlink.config.http.CORSConfiguration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.picketlink.log.BaseLog.HTTP_LOGGER;

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
 * <li>cors.allowMethods {method-list} defaults to {@code "GET, POST, HEAD, OPTIONS"}.
 * <li>cors.allowHeaders {"*"|header-list} defaults to {@code *}.
 * <li>cors.exposedHeaders {header-list} defaults to empty list.
 * <li>cors.allowCredentials {true|false} defaults to {@code true}.
 * <li>cors.maxAge {int} defaults to {@code -1} (unspecified).
 * </ul>
 *
 * @author Giriraj Sharma
 */
public class CORS {
    public static final String ORIGIN = "Origin";
    public static final String HOST = "Host";

    public static final long DEFAULT_MAX_AGE = TimeUnit.HOURS.toSeconds(1);
    public static final String DEFAULT_ALLOW_METHODS = "GET, POST, HEAD, OPTIONS";

    public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

    public static final String ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD = "*";

    public static void handleActualRequest(CORSConfiguration corsConfiguration, HttpServletRequest request,
            HttpServletResponse response) {
        HTTP_LOGGER.debugf("Processing CORS Actual Request to path [%s].", request.getRequestURI());

        final String requestOrigin = request.getHeader(ORIGIN);

        if (requestOrigin == null) {
            HTTP_LOGGER.debug("CORS origin header is null");
            throw new RuntimeException("CORS origin header is null");
        }

        Set<String> allowedOrigins = corsConfiguration.getAllowedOrigins();

        if (allowedOrigins == null
                || (!allowedOrigins.contains(requestOrigin) && !allowedOrigins.contains(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD)
                    && !corsConfiguration.isAllowAnyOrigin())) {
            HTTP_LOGGER.debug("CORS origin denied " + requestOrigin);
            throw new RuntimeException("CORS origin denied " + requestOrigin);
        }

        if (!corsConfiguration.isAllowAnyMethod()) {
            final String method = request.getMethod().toUpperCase();
            Set<String> allowedMethods = corsConfiguration.getAllowedMethods();

            if (!allowedMethods.contains(method)) {
                HTTP_LOGGER.debug("Unsupported HTTP method " + method);
                throw new RuntimeException("Unsupported HTTP method " + method);
            }
        }

        if (corsConfiguration.isAllowCredentials()) {
            response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
        } else {
            if (corsConfiguration.isAllowAnyOrigin()) {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD);
            } else {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
            }
        }

        Set<String> exposedHeaders = corsConfiguration.getExposedHeaders();

        if (exposedHeaders != null && !exposedHeaders.isEmpty()) {
            response.addHeader(ACCESS_CONTROL_EXPOSE_HEADERS, CorsUtil.join(exposedHeaders));
        }

    }

    public static void handlePreflightRequest(CORSConfiguration corsConfiguration, HttpServletRequest request,
            HttpServletResponse response) {
        HTTP_LOGGER.debugf("Processing CORS Preflight Request to path [%s].", request.getRequestURI());

        final String requestOrigin = request.getHeader(ORIGIN);

        if (requestOrigin == null) {
            HTTP_LOGGER.debug("CORS origin header is null");
            throw new RuntimeException("CORS origin header is null");
        }

        boolean allowAnyOrigin = corsConfiguration.isAllowAnyOrigin();

        if (!allowAnyOrigin) {
            Set<String> allowedOrigins = corsConfiguration.getAllowedOrigins();

            if (allowedOrigins == null
                    || (!allowedOrigins.contains(requestOrigin) && !allowedOrigins.contains(ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD) && !allowAnyOrigin)) {
                HTTP_LOGGER.debug("CORS origin denied " + requestOrigin);
                throw new RuntimeException("CORS origin denied " + requestOrigin);
            }
        }

        Set<String> allowedMethods = corsConfiguration.getAllowedMethods();

        if (!corsConfiguration.isAllowAnyMethod()) {
            String requestMethodHeader = request.getHeader(ACCESS_CONTROL_REQUEST_METHOD);
            String requestedMethod = requestMethodHeader.toUpperCase();

            if (!allowedMethods.contains(requestedMethod)) {
                HTTP_LOGGER.debug("Unsupported HTTP access control request method " + requestedMethod);
                throw new RuntimeException("Unsupported HTTP access control request method " + requestedMethod);
            }

            if (requestMethodHeader == null) {
                HTTP_LOGGER.debug("Invalid preflight CORS request: Missing Access-Control-Request-Method header");
                throw new RuntimeException("Invalid preflight CORS request: Missing Access-Control-Request-Method header");
            }
        }

        // Parse the requested author (custom) headers
        final String rawRequestHeadersString = request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS);
        final String[] requestHeaderValues = CorsUtil.parseMultipleHeaderValues(rawRequestHeadersString);
        final String[] requestHeaders = new String[requestHeaderValues.length];

        for (int i = 0; i < requestHeaders.length; i++) {
            try {
                requestHeaders[i] = CorsUtil.formatCanonical(requestHeaderValues[i]);
            } catch (IllegalArgumentException e) {
                // Invalid header name
                HTTP_LOGGER.debug("Invalid preflight CORS request: Bad request header value " + requestHeaderValues[i]);
                throw new RuntimeException("Invalid preflight CORS request: Bad request header value " + requestHeaderValues[i]);
            }
        }

        // Author request headers check
        Set<String> allowedHeaders = corsConfiguration.getAllowedHeaders();

        if (!corsConfiguration.isAllowAnyHeader()) {
            for (String requestHeader : requestHeaders) {
                if (!allowedHeaders.contains(requestHeader)) {
                    HTTP_LOGGER.debug("Unsupported HTTP access control request header " + requestHeader);
                    throw new RuntimeException("Unsupported HTTP access control request header " + requestHeader);
                }
            }
        }

        if (corsConfiguration.isAllowCredentials()) {
            response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
        } else {
            if (allowAnyOrigin) {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN_WILDCARD);
            } else {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
            }
        }

        long maxAge = corsConfiguration.getMaxAge();

        if (Long.valueOf(maxAge) != null && maxAge > 0) {
            response.addHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(maxAge));
        } else {
            response.addHeader(ACCESS_CONTROL_MAX_AGE, String.valueOf(DEFAULT_MAX_AGE));
        }

        if (allowedMethods != null && !allowedMethods.isEmpty()) {
            response.addHeader(ACCESS_CONTROL_ALLOW_METHODS, CorsUtil.join(allowedMethods));
        } else {
            response.addHeader(ACCESS_CONTROL_ALLOW_METHODS, DEFAULT_ALLOW_METHODS);
        }

        if (corsConfiguration.isAllowAnyHeader() && rawRequestHeadersString != null) {
            response.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, rawRequestHeadersString);
        } else if (allowedHeaders != null && !allowedHeaders.isEmpty()) {
            response.addHeader(ACCESS_CONTROL_ALLOW_HEADERS, CorsUtil.join(allowedHeaders));
        }

    }

}
