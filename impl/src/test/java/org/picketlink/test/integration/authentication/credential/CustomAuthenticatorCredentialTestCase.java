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

package org.picketlink.test.integration.authentication.credential;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.test.integration.ArchiveUtils;
import org.picketlink.test.integration.authentication.AbstractAuthenticationTestCase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Perform some authentication tests using a {@link CustomAuthenticator} that performs a simple authentication for a
 * custom {@link org.picketlink.idm.credential.Credentials}.
 * <p/>
 * </p>
 *
 * @author Pedro Igor
 */
public class CustomAuthenticatorCredentialTestCase extends AbstractAuthenticationTestCase {

    @Inject
    @PicketLink
    private CustomAuthenticator authenticator;

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(CustomAuthenticatorCredentialTestCase.class, CustomAuthenticator.class);
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        super.credentials.setCredential(new MyCredential("valid_token"));
        super.identity.login();

        assertTrue(super.identity.isLoggedIn());
    }

    @Test
    public void testUnsuccessfulAuthentication() throws Exception {
        super.credentials.setCredential(new MyCredential("invalid_token"));
        super.identity.login();

        assertFalse(super.identity.isLoggedIn());
    }

    @RequestScoped
    @PicketLink
    public static class CustomAuthenticator extends BaseAuthenticator {

        @Inject
        private DefaultLoginCredentials credentials;
        private boolean authenticationPerformed;

        @Override
        public void authenticate() {
            setStatus(AuthenticationStatus.FAILURE);

            if (isCredentialSupported()) {
                MyCredential credential = (MyCredential) this.credentials.getCredential();

                if ("valid_token".equals(credential.getToken())) {
                    setStatus(AuthenticationStatus.SUCCESS);
                    setAgent(new SimpleUser(USER_NAME));
                }
            }

            this.authenticationPerformed = true;
        }

        private boolean isCredentialSupported() {
            return this.credentials.getCredential() != null && MyCredential.class.equals(this.credentials.getCredential().getClass());
        }

        boolean isAuthenticationPerformed() {
            return this.authenticationPerformed;
        }

    }

    public static class MyCredential {

        private String token;

        public MyCredential(String token) {
            this.token = token;
        }

        public String getToken() {
            return this.token;
        }
    }
}