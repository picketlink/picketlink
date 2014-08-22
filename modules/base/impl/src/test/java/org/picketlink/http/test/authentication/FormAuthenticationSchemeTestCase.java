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
package org.picketlink.http.test.authentication;

import org.junit.Test;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.internal.authentication.schemes.FormAuthenticationScheme;
import org.picketlink.http.internal.authentication.schemes.support.RequestCache;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        FormAuthenticationSchemeTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class FormAuthenticationSchemeTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testFormRedirectToLogin() throws Exception {
        when(this.request.getServletPath()).thenReturn("/formProtectedUri/");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/loginFormProtectedUri.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testFormRedirectToError() throws Exception {
        when(this.request.getServletPath()).thenReturn("/formProtectedUri/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("invalid_user");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("invalid_password");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/errorFormProtectedUri.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        String savedUri = "/formProtectedUri/someUriToSave.html";

        when(this.request.getServletPath()).thenReturn(savedUri);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/loginFormProtectedUri.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath()).thenReturn("/formProtectedUri/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response).sendRedirect(CONTEXT_PATH);
    }

    @Test
    public void testSuccessfulAuthenticationPathWithoutWildCard() throws Exception {
        String savedUri = "/pathWithoutWildCard";

        when(this.request.getServletPath()).thenReturn(savedUri);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/pathWithoutWildCardLogin.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath()).thenReturn("/pathWithoutWildCard/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response).sendRedirect(CONTEXT_PATH);
    }

    @Test
    public void testRestoreOriginalRequest() throws Exception {
        String savedUri = "/formProtectedUriRestoreOriginalRequest/someUriToSave.html";

        when(this.request.getServletPath()).thenReturn(savedUri);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/loginFormProtectedUriRestoreOriginalRequest.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath())
            .thenReturn("/formProtectedUriRestoreOriginalRequest/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH + savedUri);
        verify(this.session).setAttribute(eq(RequestCache.ORIGINAL_REQUEST_ATTRIBUTE_NAME), any());
    }

    public static class SecurityConfiguration {

        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                    .forPath("/formProtectedUri/*")
                            .authenticateWith()
                                .form()
                                    .loginPage("/loginFormProtectedUri.html")
                                    .errorPage("/errorFormProtectedUri.html")
                .forPath("/formProtectedUriRestoreOriginalRequest/*")
                .authenticateWith()
                .form()
                .restoreOriginalRequest()
                .loginPage("/loginFormProtectedUriRestoreOriginalRequest.html")
                .errorPage("/loginFormProtectedUriRestoreOriginalRequest.html")
                .forPath("/pathWithoutWildCard")
                .authenticateWith()
                .form()
                .loginPage("/pathWithoutWildCardLogin.html");
        }
    }
}
