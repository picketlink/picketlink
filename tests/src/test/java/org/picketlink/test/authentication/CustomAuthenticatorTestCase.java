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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.annotations.PicketLink;
import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.junit.Assert.*;
import static org.picketlink.idm.model.basic.BasicModel.*;

/**
 * @author Pedro Igor
 *
 */
public class CustomAuthenticatorTestCase extends AbstractAuthenticationTestCase {

    @Inject
    private RelationshipManager relationshipManager;

    @Inject
    private DefaultLoginCredentials credentials;

    @Inject
    private InternalUserAuthenticator internalUserAuthenticator;

    @Inject
    private ExternalUserAuthenticator externalUserAuthenticator;

    @Deployment
    public static WebArchive deploy() {
        return deploy(CustomAuthenticatorTestCase.class);
    }

    @Before
    public void onSetup() {
        IdentityManager identityManager = getIdentityManager();

        User john = getUser(identityManager, "john");

        if (john == null) {
            john = new User("john");
            identityManager.add(john);

            Role externalUser = new Role("External User");

            identityManager.add(externalUser);

            grantRole(this.relationshipManager, john, externalUser);
        }

        User mary = getUser(identityManager, "mary");

        if (mary == null) {
            mary = new User("mary");
            identityManager.add(mary);
        }
    }
    
    @Test
    public void testSuccessfulInternalUserAuthenticator() throws Exception {
        this.credentials.setUserId("mary");

        Identity identity = getIdentity();

        identity.login();

        assertTrue(identity.isLoggedIn());
        assertEquals(Authenticator.AuthenticationStatus.SUCCESS, this.internalUserAuthenticator.getStatus());
        assertEquals(Authenticator.AuthenticationStatus.FAILURE, this.externalUserAuthenticator.getStatus());
    }

    @Test
    public void testSuccessfulExternalUserAuthenticator() throws Exception {
        this.credentials.setUserId("john");

        Identity identity = getIdentity();

        identity.login();

        assertTrue(identity.isLoggedIn());
        assertEquals(Authenticator.AuthenticationStatus.SUCCESS, this.externalUserAuthenticator.getStatus());
        assertEquals(Authenticator.AuthenticationStatus.FAILURE, this.internalUserAuthenticator.getStatus());
    }

    @RequestScoped
    public static class ExternalUserAuthenticator extends BaseAuthenticator{

        @Inject
        private DefaultLoginCredentials credentials;

        @Override
        public void authenticate() {
            setStatus(AuthenticationStatus.FAILURE);

            if ("john".equals(credentials.getUserId())) {
                setStatus(AuthenticationStatus.SUCCESS);
                setAccount(new User(credentials.getUserId()));
            }
        }
    }

    @RequestScoped
    public static class InternalUserAuthenticator extends BaseAuthenticator{

        @Inject
        private DefaultLoginCredentials credentials;

        @Override
        public void authenticate() {
            setStatus(AuthenticationStatus.FAILURE);

            if ("mary".equals(credentials.getUserId())) {
                setStatus(AuthenticationStatus.SUCCESS);
                setAccount(new User(credentials.getUserId()));
            }
        }
    }

    @RequestScoped
    public static class AuthenticatorSelector {

        @Inject
        private RelationshipManager relationshipManager;

        @Inject
        private IdentityManager identityManager;

        @Inject
        private DefaultLoginCredentials credentials;

        @Inject
        private ExternalUserAuthenticator externalUserAuthenticator;

        @Inject
        private InternalUserAuthenticator internalUserAuthenticator;

        @Produces
        @PicketLink
        public Authenticator getAuthenticator() {
            User user = getUser(this.identityManager, credentials.getUserId());

            if (user != null) {
                Role externalUser = getRole(this.identityManager, "External User");

                if (hasRole(this.relationshipManager, user, externalUser)) {
                    return this.externalUserAuthenticator;
                }
            }

            return this.internalUserAuthenticator;
        }

    }

}