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
package org.picketlink.test.integration.authentication;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.test.integration.ArchiveUtils;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author pedroigor
 */
public class AuthenticationTestCase extends AbstractAuthenticationTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(AuthenticationTestCase.class);
    }

    @Test
    public void testSuccessfulAuthentication() {
        super.credentials.setUserId(USER_NAME);
        super.credentials.setPassword(USER_PASSWORD);

        super.identity.login();

        assertTrue(super.identity.isLoggedIn());
        assertNotNull(super.identity.getAccount());
        assertEquals(super.currentAccount, super.identity.getAccount());
    }

    @Test
    public void testUnSuccessfulAuthentication() {
        super.credentials.setUserId(USER_NAME);
        super.credentials.setPassword("bad_passwd");

        super.identity.login();

        assertFalse(super.identity.isLoggedIn());
        assertNull(super.identity.getAccount());
    }
}
