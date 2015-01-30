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
    public void testActualRequestWithDefaultConfiguration() throws Exception {
        when(this.request.getServletPath()).thenReturn("/corsAuthorization");

        when(this.request.getHeader(CORS.ORIGIN)).thenReturn("https://www.example.org:9000");
        when(this.request.getMethod()).thenReturn("PUT");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).addHeader("Access-Control-Allow-Credentials", "true");
        verify(this.response, times(1)).addHeader("Access-Control-Allow-Origin", "https://www.example.org:9000");

    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) throws Exception {

            SecurityConfigurationBuilder builder = event.getBuilder();
            builder.http().forPath("/corsAuthorization")
             .cors()
             .allowedGenericHttpRequests(true)
             .allowedOrigins("https://www.example.org:9000", "http://example.com:8008")
             .supportedMethods("GET", "PUT", "HEAD", "POST", "DELETE", "OPTIONS")
             .supportedHeaders("Origin", "X-Requested-With", "Content-Type", "Accept", "Authorization")
             .exposedHeaders()
             .supportsCredentials(true)
             .maxAge(3600);
        }
    }

}
