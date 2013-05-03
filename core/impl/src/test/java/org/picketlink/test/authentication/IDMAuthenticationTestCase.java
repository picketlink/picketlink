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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.Authenticator;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.picketlink.Identity;
import org.picketlink.Identity.AuthenticationResult;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.authentication.UnexpectedCredentialException;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Perform some authentication tests using the {@link IdmAuthenticator}, which is the default {@link Authenticator}.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
@RunWith(Arquillian.class)
public class IDMAuthenticationTestCase {

    @Inject
    private Identity identity;

    @Inject
    private DefaultLoginCredentials credentials;

    @Inject
    private IdentityManager identityManager;

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap
                .create(WebArchive.class, "test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"))
                .addAsLibraries(
                        DependencyResolvers.use(MavenDependencyResolver.class).loadMetadataFromPom("pom.xml")
                                .artifact("org.picketlink:picketlink-core-impl").resolveAs(JavaArchive.class));

        archive.addClass(IDMAuthenticationTestCase.class);

        return archive;
    }

    @Before
    public void onSetup() {
        User john = this.identityManager.getUser("john");

        if (john == null) {
            john = new SimpleUser("john");
            this.identityManager.add(john);
        }

        john.setEnabled(true);

        this.identityManager.update(john);

        Password password = new Password("mypasswd");

        this.identityManager.updateCredential(john, password);
    }

    @After
    public void onFinish() {
        this.identity.logout();
    }

    @Test
    public void testSuccessfulPasswordBasedAuthentication() throws Exception {
        User john = this.identityManager.getUser("john");

        this.credentials.setPassword("mypasswd");
        this.credentials.setUserId(john.getLoginName());

        AuthenticationResult status = this.identity.login();

        assertEquals(AuthenticationResult.SUCCESS, status);
        assertTrue(this.identity.isLoggedIn());
        
        User validatedAgent = (User) this.identity.getAgent();
        
        assertNotNull(validatedAgent);
        assertEquals(john.getId(), validatedAgent.getId());
    }

    @Test
    public void testUnsuccessfulPasswordBasedAuthentication() throws Exception {
        User john = this.identityManager.getUser("john");

        this.credentials.setPassword("badpasswd");
        this.credentials.setUserId(john.getLoginName());

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
        User john = this.identityManager.getUser("john");

        this.credentials.setPassword("mypasswd");
        this.credentials.setUserId(john.getLoginName());

        this.identity.login();

        // should throw the exception. user is already authenticated.
        this.identity.login();
    }

    @Test(expected = UnexpectedCredentialException.class)
    public void failUnexpectedCredential() {
        User john = this.identityManager.getUser("john");

        this.credentials.setPassword("mypasswd");
        this.credentials.setUserId(john.getLoginName());

        this.identity.login();

        this.credentials.setUserId("invalidId");

        // should throw the exception. trying to login with a different credential.
        this.identity.login();
    }

    @Test(expected = LockedAccountException.class)
    public void failLockedAccount() {
        User john = this.identityManager.getUser("john");

        john.setEnabled(false);

        this.identityManager.update(john);

        this.credentials.setPassword("mypasswd");
        this.credentials.setUserId(john.getLoginName());

        // should throw the exception. user is disabled/locked.
        this.identity.login();
    }

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