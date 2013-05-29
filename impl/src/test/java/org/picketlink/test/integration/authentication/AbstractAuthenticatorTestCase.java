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

package org.picketlink.test.integration.authentication;

import org.junit.Before;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.Identity.AuthenticationResult;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.authentication.UnexpectedCredentialException;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.User;
import org.picketlink.test.integration.AbstractArquillianTestCase;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>Base class with some test methods that must be satisfied when testing a specific {@link Authenticator}.</p>
 * 
 * @author Pedro Igor
 *
 */
public abstract class AbstractAuthenticatorTestCase extends AbstractArquillianTestCase {
    
    protected static final String USER_NAME = "john";
    protected static final String USER_PASSWORD = "mypasswd";

    @Inject
    protected Identity identity;

    @Inject
    protected DefaultLoginCredentials credentials;
    
    @Before
    public void onFinish() {
        this.identity.logout();
    }
    
    @Test
    public void testSuccessfulPasswordBasedAuthentication() throws Exception {
        this.credentials.setPassword(USER_PASSWORD);
        this.credentials.setUserId(USER_NAME);

        AuthenticationResult status = this.identity.login();

        assertEquals(AuthenticationResult.SUCCESS, status);
        assertTrue(this.identity.isLoggedIn());
        
        User validatedAgent = (User) this.identity.getAgent();
        
        assertNotNull(validatedAgent);
        assertEquals(USER_NAME, validatedAgent.getLoginName());
    }

    @Test
    public void testUnsuccessfulPasswordBasedAuthentication() throws Exception {
        this.credentials.setUserId(USER_NAME);
        this.credentials.setPassword("badpasswd");

        AuthenticationResult status = this.identity.login();

        assertEquals(AuthenticationResult.FAILED, status);
        assertFalse(this.identity.isLoggedIn());
        
        User validatedAgent = (User) this.identity.getAgent();
        
        assertNull(validatedAgent);
    }

    @Test
    public void testEmptyCredentials() {
        this.identity.login();

        assertFalse(this.identity.isLoggedIn());
    }

    @Test(expected = UserAlreadyLoggedInException.class)
    public void failUserAlreadyLoggedIn() {
        this.credentials.setUserId(USER_NAME);
        this.credentials.setPassword(USER_PASSWORD);

        this.identity.login();

        // should throw the exception. user is already authenticated.
        this.identity.login();
    }

    @Test(expected = UnexpectedCredentialException.class)
    public void failUnexpectedCredential() {
        this.credentials.setUserId(USER_NAME);
        this.credentials.setPassword(USER_PASSWORD);

        this.identity.login();

        this.credentials.setUserId("invalidId");

        // should throw the exception. trying to login with a different credential.
        this.identity.login();
    }

    @Test(expected = LockedAccountException.class)
    public void failLockedAccount() {
        User john = doLockUserAccount();

        this.credentials.setPassword(USER_PASSWORD);
        this.credentials.setUserId(john.getLoginName());

        // should throw the exception. user is disabled/locked.
        this.identity.login();
    }

    protected abstract User doLockUserAccount();

    @Test(expected = IllegalArgumentException.class)
    public void failNullPassword() {
        // should throw the exception. password can not be null.
        this.credentials.setPassword(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failNullUserId() {
        // should throw the exception. user id can not be null.
        this.credentials.setUserId(null);
    }

}
