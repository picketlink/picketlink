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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.config.http.CORSConfiguration;
import org.picketlink.config.http.PathConfiguration;

/**
 * <p>
 * A default implementation of {@link org.picketlink.http.cors.PathCORSAuthorizer}.
 * </p>
 *
 * @author Giriraj Sharma
 */
public class CORSRequestsPathAuthorizer extends AbstractPathCORSAuthorizer {

    @Override
    protected boolean doAuthorize(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        CORSConfiguration corsConfiguration = pathConfiguration.getCORSConfiguration();
        Properties props = getProperties(corsConfiguration);

        CORSRequestConfiguration config = null;
        try {
            config = new CORSRequestConfiguration(props);
        } catch (CORSConfigurationException e) {
            printMessage(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }

        CORSRequestHandler handler = new CORSRequestHandler(config);

        CORSRequestType type = CORSRequestType.detect(request);

        // Tag if configured
        if (config.tagRequests)
            RequestTagger.tag(request, type);

        try {
            if (type.equals(CORSRequestType.ACTUAL)) {

                // Simple / actual CORS request
                handler.handleActualRequest(request, response);

                // Preserve CORS response headers on reset()
                // CORSResponseWrapper responseWrapper = new CORSResponseWrapper(response);
                // chain.doFilter(request, responseWrapper);

            } else if (type.equals(CORSRequestType.PREFLIGHT)) {

                // Preflight CORS request, handle but don't
                // pass further down the chain
                handler.handlePreflightRequest(request, response);

            } else if (config.allowGenericHttpRequests) {

                // Not a CORS request, but allow it through
                // chain.doFilter(request, response);

            } else {

                // Generic HTTP requests denied
                printMessage(response, HttpServletResponse.SC_FORBIDDEN, "Generic HTTP requests not allowed");
            }

        } catch (InvalidCORSRequestException e) {

            printMessage(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());

        } catch (CORSOriginDeniedException e) {

            String msg = e.getMessage() + ": " + e.getRequestOrigin();
            printMessage(response, HttpServletResponse.SC_FORBIDDEN, msg);

        } catch (UnsupportedHTTPMethodException e) {

            String msg = e.getMessage();

            String method = e.getRequestedMethod();

            if (method != null)
                msg = msg + ": " + method;

            printMessage(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);

        } catch (UnsupportedHTTPHeaderException e) {

            String msg = e.getMessage();

            String header = e.getRequestHeader();

            if (header != null)
                msg = msg + ": " + header;

            printMessage(response, HttpServletResponse.SC_FORBIDDEN, msg);
        }

        return true;
    }

    private Properties getProperties(CORSConfiguration corsConfiguration) {
        Properties props = new Properties();
        String key, value;

        if (Boolean.valueOf(corsConfiguration.isGenericHttpRequestsAllowed()) != null) {
            key = "cors.allowGenericHttpRequests";
            value = Boolean.valueOf(corsConfiguration.isGenericHttpRequestsAllowed()).toString();
            props.setProperty(key, value);
        }

        if (corsConfiguration.getAllowedOrigins() != null && corsConfiguration.getAllowedOrigins().length != 0) {
            key = "cors.allowOrigin";
            value = getString(corsConfiguration.getAllowedOrigins());
            props.setProperty(key, value);
        }

        if (Boolean.valueOf(corsConfiguration.isSubdomainsAllowed()) != null) {
            key = "cors.allowSubdomains";
            value = Boolean.valueOf(corsConfiguration.isSubdomainsAllowed()).toString();
            props.setProperty(key, value);
        }

        if (corsConfiguration.getSupportedMethods() != null && corsConfiguration.getSupportedMethods().length != 0) {
            key = "cors.supportedMethods";
            value = getString(corsConfiguration.getSupportedMethods());
            props.setProperty(key, value);
        }

        if (corsConfiguration.getSupportedHeaders() != null && corsConfiguration.getSupportedHeaders().length != 0) {
            key = "cors.supportedHeaders";
            value = getString(corsConfiguration.getSupportedHeaders());
            props.setProperty(key, value);
        }

        if (corsConfiguration.getExposedHeaders() != null && corsConfiguration.getExposedHeaders().length != 0) {
            key = "cors.exposedHeaders";
            value = getString(corsConfiguration.getExposedHeaders());
            props.setProperty(key, value);
        }

        if (Boolean.valueOf(corsConfiguration.isCredentialsSupported()) != null) {
            key = "cors.supportsCredentials";
            value = Boolean.valueOf(corsConfiguration.isCredentialsSupported()).toString();
            props.setProperty(key, value);
        }

        if (Integer.valueOf(corsConfiguration.getMaxAge()) != null) {
            key = "cors.maxAge";
            value = Integer.valueOf(corsConfiguration.getMaxAge()).toString();
            props.setProperty(key, value);
        }
        return props;
    }

    private void printMessage(final HttpServletResponse response, final int sc, final String msg) throws IOException,
            ServletException {

        // Set the status code
        response.setStatus(sc);

        // Write the error message
        response.resetBuffer();
        response.setContentType("text/plain");

        PrintWriter out = response.getWriter();
        out.println("Cross-Origin Resource Sharing (CORS) Filter: " + msg);
    }

    private String getString(String[] corsParam) {
        String value = "";
        for (String param : corsParam) {
            value = value + param + ", ";
        }
        return value.substring(0, value.length() - 2);
    }
}
