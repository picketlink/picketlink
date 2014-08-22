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
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.test.weld.Deployment;
import org.picketlink.http.test.SecurityInitializer;

import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        RestrictiveConfigurationTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class RestrictiveConfigurationTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testNotMappedResource() throws Exception {
        String savedUri = "/notMappedResource";

        when(this.request.getServletPath()).thenReturn(savedUri);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(0)).sendRedirect(anyString());
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testMappedResource() throws Exception {
        String savedUri = "/index.jsf";

        when(this.request.getServletPath()).thenReturn(savedUri);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq(CONTEXT_PATH + "/faces/login.xhtml"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    public static class SecurityConfiguration {

        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                .restrictive()
                .forGroup("JSF Protected Pages")
                .authenticateWith()
                .form()
                .loginPage("/faces/login.xhtml")
                .errorPage("/faces/loginFailed.xhtml")
                .forPath("/*.xhtml", "JSF Protected Pages")
                .forPath("/*.jsf", "JSF Protected Pages");
        }
    }
}
