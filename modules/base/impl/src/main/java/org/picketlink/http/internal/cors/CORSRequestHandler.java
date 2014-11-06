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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles incoming cross-origin (CORS) requests according to the configured access policy. Encapsulates the CORS processing
 * logic as specified by the <a href="http://www.w3.org/TR/2013/CR-cors-20130129/">W3C candidate recommendation</a> from
 * 2013-01-29.
 *
 * <p>
 * Note that the actual CORS exception handling (which is outside the CORS specification scope) is left to the invoking class to
 * implement.
 *
 * @author Giriraj Sharma
 */
public class CORSRequestHandler {

    /**
     * The CORS filter configuration, detailing the cross-origin access policy.
     */
    private final CORSRequestConfiguration config;

    /**
     * Pre-computed string of the CORS supported methods.
     */
    private final String supportedMethods;

    /**
     * Pre-computed string of the CORS supported headers.
     */
    private final String supportedHeaders;

    /**
     * Pre-computed string of the CORS exposed headers.
     */
    private final String exposedHeaders;

    /**
     * Creates a new CORS request handler.
     *
     * @param config Specifies the cross-origin access policy.
     */
    public CORSRequestHandler(final CORSRequestConfiguration config) {

        this.config = config;

        // Pre-compute response headers where possible

        // Access-Control-Allow-Methods
        supportedMethods = HeaderUtils.serialize(config.supportedMethods, ", ");

        // Access-Control-Allow-Headers
        if (!config.supportAnyHeader)
            supportedHeaders = HeaderUtils.serialize(config.supportedHeaders, ", ");
        else
            supportedHeaders = null;

        // / Access-Control-Expose-Headers
        exposedHeaders = HeaderUtils.serialize(config.exposedHeaders, ", ");
    }

    /**
     * Handles a simple or actual CORS request.
     *
     * <p>
     * CORS specification: <a href="http://www.w3.org/TR/2013/CR-cors-20130129/#resource-requests">Simple Cross-Origin Request,
     * Actual Request, and Redirects</a>
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     *
     * @throws InvalidCORSRequestException If not a valid CORS simple / actual request.
     * @throws CORSOriginDeniedException If the origin is not allowed.
     * @throws UnsupportedHTTPMethodException If the requested HTTP method is not supported by the CORS policy.
     */
    public void handleActualRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws InvalidCORSRequestException, CORSOriginDeniedException, UnsupportedHTTPMethodException {

        if (CORSRequestType.detect(request) != CORSRequestType.ACTUAL)
            throw new InvalidCORSRequestException("Invalid simple/actual CORS request");

        // Check origin against allow list
        Origin requestOrigin = new Origin(request.getHeader(HeaderName.ORIGIN));

        if (!config.isAllowedOrigin(requestOrigin))
            throw new CORSOriginDeniedException("CORS origin denied", requestOrigin);

        // Check method
        final String method = request.getMethod().toUpperCase();

        if (!config.isSupportedMethod(method))
            throw new UnsupportedHTTPMethodException("Unsupported HTTP method", method);

        // Success, append response headers
        if (config.supportsCredentials) {

            response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

            // The string "*" cannot be used for a resource that supports credentials.
            response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin.toString());

            response.addHeader(HeaderName.VARY, "Origin");

        } else {
            if (config.allowAnyOrigin) {
                response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            } else {
                response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin.toString());

                response.addHeader(HeaderName.VARY, "Origin");
            }
        }

        if (!exposedHeaders.isEmpty())
            response.addHeader(HeaderName.ACCESS_CONTROL_EXPOSE_HEADERS, exposedHeaders);
    }

    /**
     * Handles a preflight CORS request.
     *
     * <p>
     * CORS specification: <a href="http://www.w3.org/TR/2013/CR-cors-20130129/#resource-preflight-requests">Preflight
     * Request</a>
     *
     * @param request The HTTP request.
     * @param response The HTTP response.
     *
     * @throws InvalidCORSRequestException If not a valid CORS preflight request.
     * @throws CORSOriginDeniedException If the origin is not allowed.
     * @throws UnsupportedHTTPMethodException If the requested HTTP method is not supported by the CORS policy.
     * @throws UnsupportedHTTPHeaderException If the requested HTTP header is not supported by the CORS policy.
     */
    public void handlePreflightRequest(final HttpServletRequest request, final HttpServletResponse response)
            throws InvalidCORSRequestException, CORSOriginDeniedException, UnsupportedHTTPMethodException,
            UnsupportedHTTPHeaderException {

        if (CORSRequestType.detect(request) != CORSRequestType.PREFLIGHT)
            throw new InvalidCORSRequestException("Invalid preflight CORS request");

        // Check origin against allow list
        Origin requestOrigin = new Origin(request.getHeader(HeaderName.ORIGIN));

        if (!config.isAllowedOrigin(requestOrigin))
            throw new CORSOriginDeniedException("CORS origin denied", requestOrigin);

        // Parse requested method
        // Note: method checking must be done after header parsing, see CORS spec

        String requestMethodHeader = request.getHeader(HeaderName.ACCESS_CONTROL_REQUEST_METHOD);

        if (requestMethodHeader == null)
            throw new InvalidCORSRequestException(
                    "Invalid preflight CORS request: Missing Access-Control-Request-Method header");

        final String requestedMethod = requestMethodHeader.toUpperCase();

        // Parse the requested author (custom) headers
        final String rawRequestHeadersString = request.getHeader(HeaderName.ACCESS_CONTROL_REQUEST_HEADERS);
        final String[] requestHeaderValues = HeaderUtils.parseMultipleHeaderValues(rawRequestHeadersString);

        final String[] requestHeaders = new String[requestHeaderValues.length];

        for (int i = 0; i < requestHeaders.length; i++) {
            try {
                requestHeaders[i] = HeaderName.formatCanonical(requestHeaderValues[i]);
            } catch (IllegalArgumentException e) {
                // Invalid header name
                throw new InvalidCORSRequestException("Invalid preflight CORS request: Bad request header value");
            }
        }

        // Now, do method check
        if (!config.isSupportedMethod(requestedMethod))
            throw new UnsupportedHTTPMethodException("Unsupported HTTP method", requestedMethod);

        // Author request headers check
        if (!config.supportAnyHeader) {
            for (String requestHeader : requestHeaders) {
                if (!config.supportedHeaders.contains(requestHeader))
                    throw new UnsupportedHTTPHeaderException("Unsupported HTTP request header", requestHeader);
            }
        }

        // Success, append response headers

        if (config.supportsCredentials) {
            response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin.toString());
            response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");

            response.addHeader(HeaderName.VARY, "Origin");
        } else {
            if (config.allowAnyOrigin) {
                response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            } else {
                response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin.toString());

                response.addHeader(HeaderName.VARY, "Origin");
            }
        }

        if (config.maxAge > 0)
            response.addHeader(HeaderName.ACCESS_CONTROL_MAX_AGE, Integer.toString(config.maxAge));

        response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_METHODS, supportedMethods);

        if (config.supportAnyHeader && rawRequestHeadersString != null) {
            // Echo author headers
            response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_HEADERS, rawRequestHeadersString);

        } else if (supportedHeaders != null && !supportedHeaders.isEmpty()) {
            response.addHeader(HeaderName.ACCESS_CONTROL_ALLOW_HEADERS, supportedHeaders);
        }
    }
}
