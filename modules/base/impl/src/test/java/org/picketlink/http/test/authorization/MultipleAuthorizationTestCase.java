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
package org.picketlink.http.test.authorization;

import org.junit.Test;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.authorization.PathAuthorizer;
import org.picketlink.http.internal.authentication.schemes.FormAuthenticationScheme;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        MultipleAuthorizationTestCase.SecurityConfiguration.class, SecurityInitializer.class, MultipleAuthorizationTestCase.CustomPathAuthorizer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class MultipleAuthorizationTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testMultipleAuthorization() throws Exception {
        when(this.request.getServletPath()).thenReturn("/multipleAuthorization/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response).sendRedirect(CONTEXT_PATH);

        when(this.request.getServletPath()).thenReturn("/multipleAuthorization");
        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(0)).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testMultipleAuthorizationFailed() throws Exception {
        when(this.request.getServletPath()).thenReturn("/multipleAuthorizationFailed/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response).sendRedirect(CONTEXT_PATH);

        when(this.request.getServletPath()).thenReturn("/multipleAuthorizationFailed");
        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();
            builder
                .http()
                .allPaths()
                .authenticateWith()
                .form()
                .forPath("/multipleAuthorization")
                    .authorizeWith()
                        .role("Manager")
                        .group("Administrators")
                        .realm("default")
                .forPath("/multipleAuthorizationFailed")
                    .authorizeWith()
                        .role("Manager")
                        .group("Invalid Group");
            ;
        }
    }

    public static class CustomPathAuthorizer implements PathAuthorizer {

        @Override
        public boolean authorize(PathConfiguration pathConfiguration, HttpServletRequest request, HttpServletResponse response) {
            return request.getParameter("authz_flag") != null;
        }
    }
}
