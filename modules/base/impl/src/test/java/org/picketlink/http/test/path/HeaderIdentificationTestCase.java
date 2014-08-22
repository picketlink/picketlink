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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
        HeaderIdentificationTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class HeaderIdentificationTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testStatusCodeForbiddenWhenAjaxRequest() throws Exception {
        when(this.request.getServletPath()).thenReturn("/headerProtected/");
        when(this.request.getHeader(X_REQUESTED_WITH_HEADER_NAME)).thenReturn(X_REQUESTED_WITH_AJAX);
        when(this.request.getHeaders(X_REQUESTED_WITH_HEADER_NAME)).thenReturn(Collections.enumeration(Arrays.asList(new String[] {X_REQUESTED_WITH_AJAX})));

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"PicketLink Basic For Ajax Requests Realm\"");
        verify(this.response).sendError(SC_FORBIDDEN);
        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testRealmFromHeader() throws Exception {
        when(this.request.getServletPath()).thenReturn("/headerProtected");

        ArrayList<String> realmNames = new ArrayList<String>();

        realmNames.add("POST With Header Realm");

        when(this.request.getHeaders(eq("Realm"))).thenReturn(Collections.enumeration(realmNames));

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"POST With Header Realm\"");
    }

    @Test
    public void testNoHeader() throws Exception {
        when(this.request.getServletPath()).thenReturn("/headerProtected");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).setHeader("WWW-Authenticate", "Basic realm=\"Basic Protected Uri\"");
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                .forPath("/headerProtected/*")
                    .withHeaders()
                        .header("Realm", "POST With Header Realm")
                    .authenticateWith()
                    .basic()
                    .realmName("POST With Header Realm")
                .forPath("/headerProtected/*")
                    .authenticateWith()
                    .basic()
                    .realmName("Basic Protected Uri")
                .forPath("/headerProtected/*")
                    .withHeaders()
                    .requestedWith("XMLHttpRequest")
                    .authenticateWith()
                    .basic()
                .realmName("PicketLink Basic For Ajax Requests Realm");;
        }
    }
}
