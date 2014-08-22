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
import org.picketlink.annotations.PicketLink;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.idm.PartitionManager;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
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
        ExpressionBasedAuthorizationTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class ExpressionBasedAuthorizationTestCase extends AbstractSecurityFilterTestCase {

    @Inject
    @PicketLink
    private Instance<HttpServletRequest> picketLinkRequest;

    @Inject
    private PartitionManager partitionManager;

    @Override
    public void onBefore() throws Exception {
        super.onBefore();

        this.credentials.setUserId("picketlink");
        this.credentials.setPassword("picketlink");

        this.identity.login();

        this.credentials.setCredential(null);
    }

    @Test
    public void testAllowedExpression() throws Exception {
        when(this.request.getServletPath()).thenReturn("/onlyIfExpressionAllows");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        assertEquals("/onlyIfExpressionAllows", picketLinkRequest.get().getServletPath());
    }

    @Test
    public void testAlwaysFalseExpression() throws Exception {
        when(this.request.getServletPath()).thenReturn("/alwaysFalseExpression");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        assertEquals("/alwaysFalseExpression", picketLinkRequest.get().getServletPath());
    }

    @Test
    public void testPathExpressionAuthorizationFailed() throws Exception {
        when(this.request.getServletPath()).thenReturn("/company/single/pattern/acme/index.html");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(1)).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    @Test
    public void testPathExpressionAuthorizationSuccessful() throws Exception {
        when(this.request.getServletPath()).thenReturn("/company/single/pattern/{identity.account.partition.name}/index.html");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(0)).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testPathMultipleExpressionAuthorizationSuccessful() throws Exception {
        when(this.request.getServletPath()).thenReturn("/company/multiple/pattern/{identity.account.partition.name}/{identity.account.id}/index.html");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(0)).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testPathMultipleExpressionAuthorizationFail() throws Exception {
        when(this.request.getServletPath()).thenReturn("/company/multiple/pattern/{identity.account.partition.name}/2/index.html");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(this.response, times(1)).sendError(eq(HttpServletResponse.SC_FORBIDDEN), anyString());
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                    .forPath("/onlyIfExpressionAllows")
                            .authorizeWith()
                .expression("#{identity.account.loginName == 'picketlink'}")
                .forPath("/alwaysFalseExpression")
                .authorizeWith()
                .expression("#{hasRole('Invalid Role')}")
                .forPath("/company/single/pattern/{identity.account.partition.name}/*")
                .authorizeWith()
                .expression("#{identity.account.partition.name}")
                .forPath("/company/multiple/pattern/{identity.account.partition.name}/{identity.account.id}/*")
                .authorizeWith()
                .expression("#{identity.account.partition.name}", "#{identity.account.id}");
        }
    }
}
