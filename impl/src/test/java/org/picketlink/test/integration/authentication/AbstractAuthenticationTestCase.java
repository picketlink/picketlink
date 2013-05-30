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

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.picketlink.Identity;
import org.picketlink.authentication.internal.IdmAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.User;
import org.picketlink.test.integration.AbstractArquillianTestCase;

/**
 * <p>
 * Base class for test cases that requires authentication. By default, the {@link IdmAuthenticator} is used by default.
 * </p>
 * 
 * @author Pedro Igor
 * 
 */
public abstract class AbstractAuthenticationTestCase extends AbstractArquillianTestCase {

    protected static final String USER_NAME = "john";
    protected static final String USER_PASSWORD = "mypasswd";

    @Inject
    protected Identity identity;

    @Inject
    protected DefaultLoginCredentials credentials;

    @Inject
    protected IdentityManager identityManager;

    @Before
    public void onSetup() {
        User john = this.identityManager.getUser(USER_NAME);

        if (john == null) {
            john = new User(USER_NAME);
            this.identityManager.add(john);
        }

        john.setEnabled(true);

        this.identityManager.update(john);

        Password password = new Password(USER_PASSWORD);

        this.identityManager.updateCredential(john, password);
    }

    @After
    public void onFinish() {
        this.identity.logout();
    }

    protected void populateCredentials() {
        this.credentials.setUserId(USER_NAME);
        this.credentials.setPassword(USER_PASSWORD);
    }

}
