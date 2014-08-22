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
import org.picketlink.common.util.Base64;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.picketlink.config.http.InboundHeaderConfiguration.X_REQUESTED_WITH_AJAX;
import static org.picketlink.config.http.InboundHeaderConfiguration.X_REQUESTED_WITH_HEADER_NAME;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        BasicAuthenticationSchemeTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class BasicAuthenticationSchemeTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        when(this.request.getServletPath()).thenReturn("/basicProtectedUri/");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        assertAuthenticationRequired(SC_UNAUTHORIZED);

        prepareAuthenticationRequest("picketlink", "picketlink");

        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testStatusCodeForbiddenWhenAjaxRequest() throws Exception {
        when(this.request.getServletPath()).thenReturn("/basicProtectedUri/");
        when(this.request.getHeader(X_REQUESTED_WITH_HEADER_NAME)).thenReturn(X_REQUESTED_WITH_AJAX);
        when(this.request.getHeaders(X_REQUESTED_WITH_HEADER_NAME)).thenReturn(Collections.enumeration(Arrays.asList(new String[] {X_REQUESTED_WITH_AJAX})));

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        assertAuthenticationRequired(SC_FORBIDDEN, "PicketLink Basic For Ajax Requests Realm");

        prepareAuthenticationRequest("picketlink", "picketlink");

        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testUnSuccessfulAuthentication() throws Exception {
        when(this.request.getServletPath()).thenReturn("/basicProtectedUri/");

        prepareAuthenticationRequest("picketlink", "bad_password");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        assertAuthenticationRequired(SC_UNAUTHORIZED);
    }

    private void assertAuthenticationRequired(int expectedStatusCode, String... expectedRealm) throws IOException, ServletException {
        if (expectedRealm.length == 0) {
            expectedRealm = new String[] {"Basic Protected Uri"};
        }

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"" + expectedRealm[0] + "\"");
        verify(this.response).sendError(expectedStatusCode);
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    private void prepareAuthenticationRequest(String userName, String password) {
        when(this.request.getHeader("Authorization")).thenReturn(new String("Basic " + Base64
            .encodeBytes(String.valueOf(userName + ":" + password).getBytes())));
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                    .forPath("/basicProtectedUri/*")
                            .authenticateWith()
                                .basic()
                                    .realmName("Basic Protected Uri")
                    .forPath("/basicProtectedUri/*")
                            .withHeaders()
                                .requestedWith("XMLHttpRequest")
                            .authenticateWith()
                                .basic()
                                    .realmName("PicketLink Basic For Ajax Requests Realm");
        }
    }
}
