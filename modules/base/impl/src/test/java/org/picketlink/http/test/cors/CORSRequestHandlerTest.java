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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.picketlink.http.internal.cors.CORSOriginDeniedException;
import org.picketlink.http.internal.cors.CORSRequestConfiguration;
import org.picketlink.http.internal.cors.CORSRequestHandler;
import org.picketlink.http.internal.cors.HeaderUtils;
import org.picketlink.http.internal.cors.UnsupportedHTTPMethodException;

import junit.framework.TestCase;

/**
 * Tests the CORS request handler.
 *
 * @author Giriraj Sharma
 */
public class CORSRequestHandlerTest extends TestCase {

    public void testActualRequestWithDefaultConfiguration() throws Exception {

        CORSRequestConfiguration config = new CORSRequestConfiguration(new Properties());

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://example.com");

        MockServletResponse response = new MockServletResponse();

        handler.handleActualRequest(request, response);

        assertEquals("http://example.com", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Origin", response.getHeader("Vary"));

        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));

        assertEquals(3, response.getHeaders().size());
    }

    public void testActualRequestWithCredentialsNotAllowed() throws Exception {

        Properties props = new Properties();
        props.setProperty("cors.supportsCredentials", "false");
        CORSRequestConfiguration config = new CORSRequestConfiguration(props);

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://example.com");

        MockServletResponse response = new MockServletResponse();

        handler.handleActualRequest(request, response);

        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));

        assertNull(response.getHeader("Access-Control-Allow-Credentials"));

        assertEquals(1, response.getHeaders().size());
    }

    public void testActualRequestWithExposedHeaders() throws Exception {

        Properties props = new Properties();
        props.put("cors.exposedHeaders", "X-Custom");

        CORSRequestConfiguration config = new CORSRequestConfiguration(props);

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://example.com");

        MockServletResponse response = new MockServletResponse();

        handler.handleActualRequest(request, response);

        assertEquals("http://example.com", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Origin", response.getHeader("Vary"));

        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));

        assertEquals("X-Custom", response.getHeader("Access-Control-Expose-Headers"));

        assertEquals(4, response.getHeaders().size());
    }

    public void testActualRequestWithDeniedOrigin() throws Exception {

        Properties props = new Properties();
        props.put("cors.allowOrigin", "http://example.com");

        CORSRequestConfiguration config = new CORSRequestConfiguration(props);

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://other.com");

        MockServletResponse response = new MockServletResponse();

        try {
            handler.handleActualRequest(request, response);
            fail();
        } catch (CORSOriginDeniedException e) {
            // ok
            assertEquals("CORS origin denied", e.getMessage());
        }
    }

    public void testActualRequestWithUnsupportedMethod() throws Exception {

        Properties props = new Properties();
        props.put("cors.supportedMethods", "GET POST");

        CORSRequestConfiguration config = new CORSRequestConfiguration(props);

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://example.com");
        request.setMethod("DELETE");

        MockServletResponse response = new MockServletResponse();

        try {
            handler.handleActualRequest(request, response);
            fail();
        } catch (UnsupportedHTTPMethodException e) {
            // ok
            assertEquals("Unsupported HTTP method", e.getMessage());
        }
    }

    public void testPreflightRequestWithDefaultConfiguration() throws Exception {

        CORSRequestConfiguration config = new CORSRequestConfiguration(new Properties());

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://example.com");
        request.setHeader("Access-Control-Request-Method", "POST");
        request.setMethod("OPTIONS");

        MockServletResponse response = new MockServletResponse();

        handler.handlePreflightRequest(request, response);

        assertEquals("http://example.com", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals("Origin", response.getHeader("Vary"));

        Set<String> methods = new HashSet<String>(Arrays.asList(HeaderUtils.parseMultipleHeaderValues(response
                .getHeader("Access-Control-Allow-Methods"))));
        assertTrue(methods.contains("HEAD"));
        assertTrue(methods.contains("GET"));
        assertTrue(methods.contains("POST"));
        assertTrue(methods.contains("OPTIONS"));
        assertEquals(4, methods.size());

        assertEquals("true", response.getHeader("Access-Control-Allow-Credentials"));

        assertEquals(4, response.getHeaders().size());
    }

    public void testPreflightRequestWithCredentialsNotAllowed() throws Exception {

        Properties props = new Properties();
        props.setProperty("cors.supportsCredentials", "false");
        CORSRequestConfiguration config = new CORSRequestConfiguration(props);

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://example.com");
        request.setHeader("Access-Control-Request-Method", "POST");
        request.setMethod("OPTIONS");

        MockServletResponse response = new MockServletResponse();

        handler.handlePreflightRequest(request, response);

        assertEquals("*", response.getHeader("Access-Control-Allow-Origin"));

        Set<String> methods = new HashSet<String>(Arrays.asList(HeaderUtils.parseMultipleHeaderValues(response
                .getHeader("Access-Control-Allow-Methods"))));
        assertTrue(methods.contains("HEAD"));
        assertTrue(methods.contains("GET"));
        assertTrue(methods.contains("POST"));
        assertTrue(methods.contains("OPTIONS"));
        assertEquals(4, methods.size());

        assertNull(response.getHeader("Access-Control-Allow-Credentials"));

        assertEquals(2, response.getHeaders().size());
    }

    public void testPreflightRequestWithSupportAnyHeader() throws Exception {

        Properties props = new Properties();
        props.setProperty("cors.supportedHeaders", "*");

        CORSRequestConfiguration config = new CORSRequestConfiguration(props);

        CORSRequestHandler handler = new CORSRequestHandler(config);

        MockServletRequest request = new MockServletRequest();
        request.setHeader("Origin", "http://example.com");
        request.setHeader("Access-Control-Request-Method", "POST");
        request.setHeader("Access-Control-Request-Headers", "Authorization, Content-Type");
        request.setMethod("OPTIONS");

        MockServletResponse response = new MockServletResponse();

        handler.handlePreflightRequest(request, response);

        assertEquals("Authorization, Content-Type", response.getHeader("Access-Control-Allow-Headers"));
    }
}
