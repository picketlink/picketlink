/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.test.authentication.credential;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.test.authentication.AbstractAuthenticationTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class UsernamePasswordCredentialTestCase extends AbstractAuthenticationTestCase {

    @Deployment
    public static WebArchive deploy() {
        return deploy(UsernamePasswordCredentialTestCase.class);
    }

    @Test
    public void testSuccessfullAuthentication() {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId(USER_NAME);
        credentials.setPassword(USER_PASSWORD);

        Identity identity = getIdentity();

        identity.login();

        assertTrue(identity.isLoggedIn());
        assertEquals(getAccount(), identity.getAccount());
    }

    @Test
    public void testUnsuccessfullAuthentication() {
        DefaultLoginCredentials credentials = getCredentials();

        credentials.setUserId("bad_user");
        credentials.setPassword(USER_PASSWORD);

        Identity identity = getIdentity();

        identity.login();

        assertFalse(identity.isLoggedIn());

        credentials.setUserId(USER_NAME);
        credentials.setPassword("bad_password");

        identity.login();

        assertFalse(identity.isLoggedIn());
    }

    @Test(expected = IllegalArgumentException.class)
    public void failNullPassword() {
        // should throw the exception. password can not be null.
        getCredentials().setPassword(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failNullUserId() {
        // should throw the exception. user id can not be null.
        getCredentials().setUserId(null);
    }
}
