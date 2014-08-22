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
package org.picketlink.http.test.path;

import org.junit.Test;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.HttpMethod;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        MethodIdentificationTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class MethodIdentificationTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testGetMethod() throws Exception {
        when(this.request.getServletPath()).thenReturn("/methodProtected");
        when(this.request.getMethod()).thenReturn("GET");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"GET Method Realm\"");
    }

    @Test
    public void testPostMethod() throws Exception {
        when(this.request.getServletPath()).thenReturn("/methodProtected");
        when(this.request.getMethod()).thenReturn("POST");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"POST Method\"");
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                .forPath("/methodProtected")
                .withMethod(HttpMethod.GET)
                .authenticateWith()
                .basic()
                .realmName("GET Method Realm")
                .forPath("/methodProtected")
                .withMethod(HttpMethod.POST)
                .authenticateWith()
                .basic()
                .realmName("POST Method");
        }
    }
}
