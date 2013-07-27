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

package org.picketlink.test.authentication;

import java.net.Authenticator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Account;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.Identity.AuthenticationResult;

/**
 * <p>
 * Perform some authentication tests using the {@link IdmAuthenticator}, which is the default {@link Authenticator}.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public class DefaultAuthenticationTestCase extends AbstractAuthenticationTestCase {

    @Deployment
    public static WebArchive deploy() {
        return deploy(DefaultAuthenticationTestCase.class);
    }
    
    @Test
    public void testSuccessfulPasswordBasedAuthentication() throws Exception {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setPassword(USER_PASSWORD);
        credentials.setUserId(USER_NAME);

        Identity identity = getIdentity();

        AuthenticationResult status = identity.login();

        assertEquals(AuthenticationResult.SUCCESS, status);
        assertTrue(identity.isLoggedIn());

        assertEquals(getAccount(), identity.getAccount());
    }

    @Test
    public void testUnsuccessfulPasswordBasedAuthentication() throws Exception {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId(USER_NAME);
        credentials.setPassword("badpasswd");

        Identity identity = getIdentity();

        AuthenticationResult status = identity.login();

        assertEquals(AuthenticationResult.FAILED, status);
        assertFalse(identity.isLoggedIn());

        assertNull(identity.getAccount());
    }

    @Test
    public void testEmptyCredentials() {
        Identity identity = getIdentity();

        identity.login();

        assertFalse(identity.isLoggedIn());
    }

    @Test(expected = UserAlreadyLoggedInException.class)
    public void failUserAlreadyLoggedIn() {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId(USER_NAME);
        credentials.setPassword(USER_PASSWORD);

        Identity identity = getIdentity();

        identity.login();

        // should throw the exception. user is already authenticated.
        identity.login();
    }

    @Test(expected = LockedAccountException.class)
    public void failLockedAccount() {
        Account account = getAccount();

        account.setEnabled(false);

        getIdentityManager().update(account);

        DefaultLoginCredentials credentials = getCredentials();

        credentials.setPassword(USER_PASSWORD);
        credentials.setUserId(USER_NAME);

        // should throw the exception. user is disabled/locked.
        Identity identity = getIdentity();

        identity.login();
    }
    
}