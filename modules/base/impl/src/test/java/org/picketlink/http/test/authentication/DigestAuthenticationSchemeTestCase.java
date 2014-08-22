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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.internal.authentication.schemes.support.HTTPDigestUtil;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.http.test.SecurityInitializer;
import org.picketlink.idm.credential.Digest;
import org.picketlink.test.weld.Deployment;

import javax.enterprise.event.Observes;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        DigestAuthenticationSchemeTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class DigestAuthenticationSchemeTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        when(this.request.getServletPath()).thenReturn("/digestProtectedUri/");

        final List<String> digest = new ArrayList<String>();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                digest.add(invocation.getArguments()[1].toString());
                return null;
            }
        }).when(response).setHeader(anyString(), anyString());

        when(this.request.getMethod()).thenReturn("GET");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        assertAuthenticationRequired(SC_UNAUTHORIZED);

        prepareAuthenticationRequest("picketlink", "picketlink", digest.get(0));

        reset(this.response);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        Mockito.verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    private void assertAuthenticationRequired(int expectedStatusCode, String... expectedRealm) throws IOException, ServletException {
        if (expectedRealm.length == 0) {
            expectedRealm = new String[] {"PicketLink Test DIGEST Realm"};
        }

        Mockito.verify(this.response).setHeader(eq("WWW-Authenticate"), Mockito.contains("Digest realm=\"" + expectedRealm[0] + "\""));
        Mockito.verify(this.response).sendError(expectedStatusCode);
        Mockito.verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    private String buildAuthorizationHeader(Digest digest, String userName, String password) {
        String clientResponse = null;

        digest.setUsername(userName);
        digest.setMethod("GET");
        digest.setUri("/digestProtectedUri/");
        digest.setNonce(digest.getNonce());
        digest.setClientNonce(digest.getNonce());
        digest.setNonceCount("00001");

        clientResponse = HTTPDigestUtil.clientResponseValue(digest, password.toCharArray());

        StringBuilder str = new StringBuilder();

        str.append("Digest ")
            .append("username=\"").append(digest.getUsername()).append("\",")
            .append("realm=\"").append(digest.getRealm()).append("\",")
            .append("nonce=\"").append(digest.getNonce()).append("\",")
            .append("cnonce=\"").append(digest.getClientNonce()).append("\",")
            .append("uri=\"").append(digest.getUri()).append("\",")
            .append("qop=").append(digest.getQop()).append(",")
            .append("nc=").append(digest.getNonceCount()).append(",")
            .append("response=\"").append(clientResponse).append("\"");

        return str.toString();
    }

    private void prepareAuthenticationRequest(String userName, String password, String authenticateHeader) {
        String[] challengeTokens = HTTPDigestUtil.quoteTokenize(authenticateHeader.toString().replace("Digest ", ""));
        Digest clientDigest = HTTPDigestUtil.digest(challengeTokens);

        when(this.request.getHeader("Authorization")).thenReturn(buildAuthorizationHeader(clientDigest, userName, password));
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                .forPath("/digestProtectedUri/*")
                .authenticateWith()
                .digest()
                .realmName("PicketLink Test DIGEST Realm");
        }
    }
}
