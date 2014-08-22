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
import org.picketlink.config.http.AuthenticationSchemeConfiguration;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.authentication.HttpAuthenticationScheme;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        CustomAuthenticationSchemeTestCase.SecurityConfiguration.class, SecurityInitializer.class, CustomAuthenticationSchemeTestCase.CustomAuthenticationScheme.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class CustomAuthenticationSchemeTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testChallengeCredential() throws Exception {
        when(this.request.getServletPath()).thenReturn("/customAuthenticationScheme/teste");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.request, times(1)).setAttribute("challengeClient", true);
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(1)).sendRedirect("/customLogin");
    }

    @Test
    public void testExtractCredential() throws Exception {
        when(this.request.getServletPath()).thenReturn("/customAuthenticationScheme/teste");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(1)).sendRedirect("/customLogin");
    }

    @Test
    public void testOnPostAuthentication() throws Exception {
        when(this.request.getServletPath()).thenReturn("/customAuthenticationScheme/teste");
        when(this.request.getParameter("force_login")).thenReturn("true");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.request, times(1)).setAttribute("onPostAuthentication", true);
        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    public static class SecurityConfiguration {

        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                    .forPath("/customAuthenticationScheme/*")
                            .authenticateWith()
                                .scheme(CustomAuthenticationScheme.class);
        }
    }

    public static class CustomAuthenticationScheme implements HttpAuthenticationScheme {

        @Override
        public void initialize(AuthenticationSchemeConfiguration config) {

        }

        @Override
        public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
            if (request.getParameter("force_login") != null) {
                UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

                credential.setUsername("picketlink");
                credential.setPassword(new Password("picketlink"));

                creds.setCredential(credential);
            } else {
                request.setAttribute("extractCredential", true);
            }
        }

        @Override
        public void challengeClient(HttpServletRequest request, HttpServletResponse response) {
            request.setAttribute("challengeClient", true);

            try {
                response.sendRedirect("/customLogin");
            } catch (Exception e) {
                throw new RuntimeException("Could not challenge client credentials.", e);
            }
        }

        @Override
        public void onPostAuthentication(HttpServletRequest request, HttpServletResponse response) {
            request.setAttribute("onPostAuthentication", true);
        }
    }
}
