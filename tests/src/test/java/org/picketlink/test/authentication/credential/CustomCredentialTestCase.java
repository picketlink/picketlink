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

package org.picketlink.test.authentication.credential;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.authentication.AbstractAuthenticationTestCase;
import static org.junit.Assert.assertEquals;
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
public class CustomCredentialTestCase extends AbstractAuthenticationTestCase {

    public static final String VALID_TOKEN = "valid_token";

    @Deployment
    public static WebArchive deploy() {
        return deploy(CustomCredentialTestCase.class);
    }

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setCredential(new MyCredential(VALID_TOKEN));

        Identity identity = getIdentity();

        identity.login();

        assertTrue(identity.isLoggedIn());
        assertEquals(USER_NAME, ((Agent) identity.getAccount()).getLoginName());
    }

    @Test
    public void testUnsuccessfulAuthentication() throws Exception {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setCredential(new MyCredential("invalid_token"));

        Identity identity = getIdentity();

        identity.login();

        assertFalse(identity.isLoggedIn());
    }

    @RequestScoped
    @PicketLink
    public static class CustomAuthenticator extends BaseAuthenticator {

        @Inject
        private DefaultLoginCredentials credentials;

        @Override
        public void authenticate() {
            setStatus(AuthenticationStatus.FAILURE);

            if (isCredentialSupported()) {
                MyCredential credential = (MyCredential) this.credentials.getCredential();

                if (VALID_TOKEN.equals(credential.getToken())) {
                    setStatus(AuthenticationStatus.SUCCESS);
                    setAccount(new User(USER_NAME));
                }
            }
        }

        private boolean isCredentialSupported() {
            return this.credentials.getCredential() != null
                    && MyCredential.class.equals(this.credentials.getCredential().getClass());
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