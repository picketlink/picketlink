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
import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.internal.DigestUtil;
import org.picketlink.idm.model.Realm;
import org.picketlink.test.integration.ArchiveUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class DigestAuthenticationTestCase extends AbstractAuthenticationTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(DigestAuthenticationTestCase.class);
    }

    @Test
    public void testSuccessfullAuthentication() {
        updateDigestCredential();

        Digest credential = new Digest();

        credential.setUsername(USER_NAME);
        credential.setRealm(Realm.DEFAULT_REALM);
        credential.setDigest(DigestUtil.calculateA1(USER_NAME, Realm.DEFAULT_REALM, USER_PASSWORD.toCharArray()));

        super.credentials.setCredential(credential);
        super.identity.login();

        assertTrue(super.identity.isLoggedIn());
    }

    @Test
    public void testUnsuccessfullAuthentication() {
        updateDigestCredential();

        Digest credential = new Digest();

        credential.setUsername(USER_NAME);
        credential.setRealm("Another Realm");
        credential.setDigest(DigestUtil.calculateA1(USER_NAME, Realm.DEFAULT_REALM, USER_PASSWORD.toCharArray()));

        super.credentials.setCredential(credential);
        super.identity.login();

        assertFalse(super.identity.isLoggedIn());

        credential.setUsername(USER_NAME);
        credential.setRealm(Realm.DEFAULT_REALM);
        credential.setDigest(DigestUtil.calculateA1(USER_NAME, Realm.DEFAULT_REALM, "bad_password".toCharArray()));

        super.credentials.setCredential(credential);
        super.identity.login();

        assertFalse(super.identity.isLoggedIn());

        credential.setUsername(USER_NAME);
        credential.setRealm(Realm.DEFAULT_REALM);
        credential.setDigest(DigestUtil.calculateA1("bad_username", Realm.DEFAULT_REALM, USER_PASSWORD.toCharArray()));

        super.credentials.setCredential(credential);
        super.identity.login();

        assertFalse(super.identity.isLoggedIn());
    }

    private void updateDigestCredential() {
        Digest credential = new Digest();

        credential.setRealm(Realm.DEFAULT_REALM);
        credential.setUsername(USER_NAME);
        credential.setPassword(USER_PASSWORD);

        super.identityManager.updateCredential(super.identityManager.getUser(USER_NAME), credential);
    }

}
