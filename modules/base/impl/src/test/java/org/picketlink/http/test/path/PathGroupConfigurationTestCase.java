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
import org.picketlink.http.internal.authentication.schemes.FormAuthenticationScheme;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
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
        PathGroupConfigurationTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class PathGroupConfigurationTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testAuthenticationFromGroup() throws Exception {
        when(this.request.getServletPath()).thenReturn("/anyUrl/anyResource.jsp");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath()).thenReturn("/formAuthenticationGroupUri/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH);
    }

    @Test
    public void testAuthorizationRequired() throws Exception {
        when(this.request.getServletPath()).thenReturn("/noGroupPath");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(0)).sendRedirect(anyString());
        verify(this.response, times(1)).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testOverridingAuthorizationFromGroup() throws Exception {
        when(this.request.getServletPath()).thenReturn("/overrideAuthorization");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath()).thenReturn("/formAuthenticationGroupUri/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH);

        reset(this.response);
        when(this.request.getServletPath()).thenReturn("/overrideAuthorization");
        this.credentials.setCredential(null);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testOverrideAuthenticationFromGroup() throws Exception {
        when(this.request.getServletPath()).thenReturn("/overrideAuthentication");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"PicketLink Default Realm\"");
    }

    @Test
    public void testOverrideMethodsFromGroup() throws Exception {
        when(this.request.getServletPath()).thenReturn("/overrideMethod");
        when(this.request.getMethod()).thenReturn(HttpMethod.POST.name());

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath()).thenReturn("/overrideMethod/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH);

        reset(this.response);
        when(this.request.getServletPath()).thenReturn("/overrideMethod");
        when(this.request.getMethod()).thenReturn(HttpMethod.GET.name());

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(1)).sendError(eq(HttpServletResponse.SC_METHOD_NOT_ALLOWED), anyString());
    }

    @Test
    public void testInheritRedirectConfiguration() throws Exception {
        when(this.request.getServletPath()).thenReturn("/onlyAcmeRealmName");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath()).thenReturn("/onlyAcmeRealmName/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        when(this.request.getServletPath()).thenReturn("/onlyAcmeRealmName");
        reset(this.response);
        this.credentials.setCredential(null);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(0)).sendError(anyInt());
        verify(this.response, times(1)).sendRedirect(CONTEXT_PATH + "/forbidden.html");

        when(this.request.getServletPath()).thenReturn("/onlyAcmeRealmName");
        when(this.request.getMethod()).thenThrow(new RuntimeException("Simulating Exception"));
        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(0)).sendError(anyInt());
        verify(this.response, times(1)).sendRedirect(CONTEXT_PATH + "/error.html");
    }

    @Test
    public void testInheritRedirectSuccessConfiguration() throws Exception {
        when(this.request.getServletPath()).thenReturn("/onlyAcmeRealmName");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getServletPath()).thenReturn("/onlyAcmeRealmName/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        when(this.request.getServletPath()).thenReturn("/logout");
        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(0)).sendError(anyInt());
        verify(this.response, times(1)).sendRedirect(CONTEXT_PATH + "/success.html");
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();
            String groupName = "FORM Authentication";

            builder
                .http()
                .forGroup(groupName)
                        .authenticateWith()
                            .form()
                        .authorizeWith()
                            .role("Manager")
                .forPath("/*", groupName)
                .forPath("/overrideAuthorization", groupName)
                        .authorizeWith()
                            .role("Invalid Role")
                .forPath("/overrideAuthentication", groupName)
                .authenticateWith()
                .basic()
                .forPath("/overrideMethod", groupName)
                        .withMethod(HttpMethod.POST)
                .forPath("/noGroupPath")
                        .authorizeWith()
                            .role("Some Role")
                .forGroup("Inherit Redirect Config")
                .authenticateWith()
                .form()
                .authorizeWith()
                .realm("Acme")
                .redirectTo("/forbidden.html").whenForbidden()
                .redirectTo("/error.html").whenError()
                .redirectTo("/success.html")
                .forPath("/onlyAcmeRealmName", "Inherit Redirect Config")
                .forPath("/logout", "Inherit Redirect Config")
                .logout();
        }
    }
}
