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
import org.picketlink.web.HttpMethod;
import org.picketlink.http.internal.schemes.FormAuthenticationScheme;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.test.weld.Deployment;
import org.picketlink.http.test.SecurityInitializer;

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
        PathGroupConfigurationTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class PathGroupConfigurationTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testAuthenticationFromGroup() throws Exception {
        when(this.request.getRequestURI()).thenReturn("/anyUrl/anyResource.jsp");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getRequestURI()).thenReturn("/formAuthenticationGroupUri/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH);
    }

    @Test
    public void testNotUsingGroup() throws Exception {
        when(this.request.getRequestURI()).thenReturn("/noGroupPath");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(0)).sendRedirect(anyString());
        verify(this.response, times(1)).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testOverridingAuthorizationFromGroup() throws Exception {
        when(this.request.getRequestURI()).thenReturn("/overrideAuthorization");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getRequestURI()).thenReturn("/formAuthenticationGroupUri/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH);

        reset(this.response);
        when(this.request.getRequestURI()).thenReturn("/overrideAuthorization");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response, times(1)).sendError(HttpServletResponse.SC_FORBIDDEN);
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testOverrideAuthenticationFromGroup() throws Exception {
        when(this.request.getRequestURI()).thenReturn("/overrideAuthentication");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"PicketLink Default Realm\"");
    }

    @Test
    public void testOverrideMethodsFromGroup() throws Exception {
        when(this.request.getRequestURI()).thenReturn("/overrideMethod");
        when(this.request.getMethod()).thenReturn(HttpMethod.POST.name());

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(eq("/picketlink-app/login.html"));
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        when(this.request.getRequestURI()).thenReturn("/formAuthenticationGroupUri/" + FormAuthenticationScheme.J_SECURITY_CHECK);
        when(this.request.getParameter(FormAuthenticationScheme.J_USERNAME)).thenReturn("picketlink");
        when(this.request.getParameter(FormAuthenticationScheme.J_PASSWORD)).thenReturn("picketlink");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH);

        reset(this.response);
        when(this.request.getRequestURI()).thenReturn("/overrideMethod");
        when(this.request.getMethod()).thenReturn(HttpMethod.GET.name());

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(1)).sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();
            String groupName = "FORM Authentication";

            builder
                .http()
                .pathGroup(groupName)
                .inbound()
                .authc()
                .form()
                .authz()
                    .allowedRoles("Manager")
                .path("/*", groupName)
                .path("/overrideAuthorization", groupName)
                    .inbound()
                        .authz()
                            .allowedRoles("Invalid Role")
                .path("/overrideAuthentication", groupName)
                .inbound()
                .authc()
                .basic()
                .path("/overrideMethod", groupName)
                    .inbound()
                        .methods("POST")
                .path("/noGroupPath")
                    .inbound()
                        .authz()
                            .allowedRoles("Some Role");
        }
    }
}
