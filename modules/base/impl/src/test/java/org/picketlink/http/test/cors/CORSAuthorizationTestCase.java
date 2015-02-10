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
package org.picketlink.http.test.cors;

import org.junit.Test;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.internal.cors.CORS;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the CORS requests via SecurityFilter.
 *
 * @author Giriraj Sharma
 */
@Deployment(beans = { CORSAuthorizationTestCase.SecurityConfiguration.class, SecurityInitializer.class }, excludeBeansFromPackage = "org.picketlink.http.test")
public class CORSAuthorizationTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testSimpleActualRequests() throws Exception {
        // Actual Request with one of allowed origin
        when(this.request.getServletPath()).thenReturn("/corsAuthorization");

        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.org:9000");
        when(this.request.getMethod()).thenReturn("PUT");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        verify(this.response, times(1)).addHeader("Access-Control-Allow-Credentials", "true");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Origin", "http://www.example.org:9000");
        verify(this.response, times(1)).addHeader("Access-Control-Expose-Headers", "Origin, Accept");
        verify(this.response, times(0)).addHeader("Access-Control-Max-Age", "3600");
        verify(this.response, times(0)).addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT");
        verify(this.response, times(0)).addHeader("Access-Control-Allow-Headers",
                "Authorization, X-Requested-With, Origin, Accept, Content-Type");

        // Actual Request with another allowed origin
        reset(this.response);
        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.com:8008");
        when(this.request.getMethod()).thenReturn("GET");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(2)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        verify(this.response, times(1)).addHeader("Access-Control-Allow-Credentials", "true");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Origin", "http://www.example.com:8008");
        verify(this.response, times(1)).addHeader("Access-Control-Expose-Headers", "Origin, Accept");
        verify(this.response, times(0)).addHeader("Access-Control-Max-Age", "3600");
        verify(this.response, times(0)).addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT");
        verify(this.response, times(0)).addHeader("Access-Control-Allow-Headers",
                "Authorization, X-Requested-With, Origin, Accept, Content-Type");

    }

    @Test
    public void testSimplePreflightRequests() throws Exception {

        // Preflight Request with one of allowed origin
        when(this.request.getServletPath()).thenReturn("/corsAuthorization");

        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.org:9000");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("GET");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Content-Type, Accept");
        when(this.request.getMethod()).thenReturn("OPTIONS");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        verify(this.response, times(1)).addHeader("Access-Control-Allow-Credentials", "true");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Origin", "http://www.example.org:9000");
        verify(this.response, times(0)).addHeader("Access-Control-Expose-Headers", "Origin, Accept");
        verify(this.response, times(1)).addHeader("Access-Control-Max-Age", "3600");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Headers",
                "Authorization, X-Requested-With, Origin, Accept, Content-Type");

        // Preflight Request with another allowed origin
        reset(this.response);
        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.com:8008");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("POST");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Authorization, Origin");
        when(this.request.getMethod()).thenReturn("OPTIONS");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        verify(this.response, times(1)).addHeader("Access-Control-Allow-Credentials", "true");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Origin", "http://www.example.com:8008");
        verify(this.response, times(0)).addHeader("Access-Control-Expose-Headers", "Origin, Accept");
        verify(this.response, times(1)).addHeader("Access-Control-Max-Age", "3600");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Headers",
                "Authorization, X-Requested-With, Origin, Accept, Content-Type");

    }

    @Test
    public void testOtherRequest() throws Exception {

        // Other request (CORS headers are not added)
        when(this.request.getServletPath()).thenReturn("/corsAuthorization");

        when(this.request.getHeader(CORS.ORIGIN)).thenReturn(null);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(0)).addHeader("Access-Control-Allow-Credentials", "true");
        verify(this.response, times(0)).addHeader("Access-Control-Allow-Origin", "http://www.example.com:8008");
        verify(this.response, times(0)).addHeader("Access-Control-Expose-Headers", "Origin, Accept");
        verify(this.response, times(0)).addHeader("Access-Control-Max-Age", "3600");
        verify(this.response, times(0)).addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, OPTIONS, PUT");
        verify(this.response, times(0)).addHeader("Access-Control-Allow-Headers",
                "Authorization, X-Requested-With, Origin, Accept, Content-Type");

    }

    @Test
    public void testInvalidActualRequests() throws Exception {

        // Actual request CORS origin denied
        when(this.request.getServletPath()).thenReturn("/corsAuthorization");

        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.com:9000");
        when(this.request.getMethod()).thenReturn("PUT");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(500, "CORS origin denied http://www.example.com:9000");

        // Actual request Unsupported HTTP method
        reset(this.response);
        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.com:8008");
        when(this.request.getMethod()).thenReturn("HEAD");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(500, "Unsupported HTTP method HEAD");

    }

    @Test
    public void testInvalidPreflightRequests() throws Exception {

        // Preflight request CORS origin denied
        when(this.request.getServletPath()).thenReturn("/corsAuthorization");

        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.com:9000");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("GET");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Content-Type, Accept");
        when(this.request.getMethod()).thenReturn("OPTIONS");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(500, "CORS origin denied http://www.example.com:9000");

        // Preflight request Unsupported HTTP method HEAD
        reset(this.response);
        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.org:9000");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("HEAD");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Content-Type, Accept");
        when(this.request.getMethod()).thenReturn("OPTIONS");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(500, "Unsupported HTTP access control request method HEAD");

        // Preflight request Unsupported HTTP header Invalid-Header-Value
        reset(this.response);
        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.org:9000");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("POST");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Content-Type, Invalid-Header-Value");
        when(this.request.getMethod()).thenReturn("OPTIONS");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(500, "Unsupported HTTP access control request header Invalid-Header-Value");

        // Preflight request Unsupported HTTP header format(s)
        reset(this.response);
        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("http://www.example.org:9000");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_METHOD)).thenReturn("POST");
        when(this.request.getHeader(CORS.ACCESS_CONTROL_REQUEST_HEADERS)).thenReturn("Content-Type, X-r@b");
        when(this.request.getMethod()).thenReturn("OPTIONS");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(500, "Invalid preflight CORS request: Bad request header value X-r@b");


    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) throws Exception {

            SecurityConfigurationBuilder builder = event.getBuilder();
            builder.http().forPath("/corsAuthorization")
            .cors()
            .allowedOrigins("http://www.example.org:9000", "http://www.example.com:8008")
            .supportedMethods("GET", "PUT", "POST", "DELETE", "OPTIONS")
            .supportedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization")
            .exposedHeaders("Origin", "Accept")
            .supportsCredentials(true).maxAge(3600);
        }
    }

}
