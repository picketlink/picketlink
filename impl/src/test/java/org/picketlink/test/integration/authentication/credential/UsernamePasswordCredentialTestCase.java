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
package org.picketlink.test.integration.authentication.credential;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.idm.credential.Password;
import org.picketlink.test.integration.ArchiveUtils;
import org.picketlink.test.integration.authentication.AbstractAuthenticationTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class UsernamePasswordCredentialTestCase extends AbstractAuthenticationTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(UsernamePasswordCredentialTestCase.class);
    }

    @Test
    public void testSuccessfullAuthentication() {
        updateUsernamePasswordCredential();

        super.credentials.setUserId(USER_NAME);
        super.credentials.setPassword(USER_PASSWORD);
        super.identity.login();

        assertTrue(super.identity.isLoggedIn());
        assertNotNull(super.identity.getAgent());
        assertEquals(USER_NAME, super.identity.getAgent().getLoginName());
    }

    @Test
    public void testUnsuccessfullAuthentication() {
        updateUsernamePasswordCredential();

        super.credentials.setUserId("bad_user");
        super.credentials.setPassword(USER_PASSWORD);
        super.identity.login();

        assertFalse(super.identity.isLoggedIn());

        super.credentials.setUserId(USER_NAME);
        super.credentials.setPassword("bad_password");
        super.identity.login();

        assertFalse(super.identity.isLoggedIn());
        assertNull(super.identity.getAgent());
    }

    private void updateUsernamePasswordCredential() {
        super.identityManager.updateCredential(super.identityManager.getUser(USER_NAME), new Password(USER_PASSWORD));
    }

}
