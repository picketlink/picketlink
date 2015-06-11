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
package org.picketlink.http.test.outbound;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.authentication.CredentialExpiredException;
import org.picketlink.authentication.event.LoggedInEvent;
import org.picketlink.common.util.Base64;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        RedirectionAfterCustomExceptionTestCase.SecurityConfiguration.class,
            CustomExceptionThrower.class,
            SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class RedirectionAfterCustomExceptionTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testRedirectWhenCredentialExpiredExceptionIsThrown() throws Exception {
        when(this.request.getServletPath()).thenReturn("/basicProtectedUri/");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        when(this.request.getHeader("Authorization")).thenReturn(new String("Basic " + Base64
                .encodeBytes(String.valueOf("picketlink:picketlink").getBytes())));

        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        verify(this.response).sendRedirect(CONTEXT_PATH + "/customErrorPage.html");
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                    .forPath("/basicProtectedUri/*")
                        .authenticateWith()
                            .basic()
                                .redirectTo("/customErrorPage.html")
                                    .whenException(CustomExceptionThrower.CustomException.class);

        }
    }
}
