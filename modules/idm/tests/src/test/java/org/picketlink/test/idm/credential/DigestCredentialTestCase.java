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

package org.picketlink.test.idm.credential;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.storage.DigestCredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.credential.util.DigestUtil;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for {@link DigestCredentials} type.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class DigestCredentialTestCase extends AbstractPartitionManagerTestCase {

    public DigestCredentialTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(user.getLoginName());
        digestPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, digestPassword);
        
        digestPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));
        
        DigestCredentials credential = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
        assertEquals(user.getId(), credential.getValidatedAccount().getId());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(user.getLoginName());
        digestPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, digestPassword);
        
        digestPassword.setDigest(DigestUtil.calculateA1("Bad" + user.getLoginName(), digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));
        
        DigestCredentials badUserName = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(badUserName);

        assertEquals(Status.INVALID, badUserName.getStatus());
        assertNull(badUserName.getValidatedAccount());

        digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(user.getLoginName());
        digestPassword.setPassword("bad_somePassword");
        
        digestPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));
        
        DigestCredentials badPassword = new DigestCredentials(digestPassword);
        
        identityManager.validateCredentials(badPassword);

        assertEquals(Status.INVALID, badPassword.getStatus());
        assertNull(badPassword.getValidatedAccount());

    }
    
    @Test
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digest = new Digest();
        
        digest.setRealm("pl-idm");
        digest.setUsername(user.getLoginName());
        digest.setPassword("somePassword");
        
        Calendar expirationDate = Calendar.getInstance();
        
        expirationDate.add(Calendar.MINUTE, -1);
        
        identityManager.updateCredential(user, digest, new Date(), expirationDate.getTime());

        DigestCredentials credential = new DigestCredentials(digest);

        digest.setDigest(DigestUtil.calculateA1(user.getLoginName(), digest.getRealm(), digest.getPassword().toCharArray()));
        
        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());
        
        Digest newPassword = new Digest();
        
        newPassword.setRealm("pl-idm");
        newPassword.setUsername(user.getLoginName());
        newPassword.setPassword("someNewPassword");
        
        identityManager.updateCredential(user, newPassword);
        
        credential = new DigestCredentials(newPassword);
        
        newPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), newPassword.getRealm(), newPassword.getPassword().toCharArray()));
        
        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    public void testResetCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digest = new Digest();

        digest.setRealm("pl-idm");
        digest.setUsername(user.getLoginName());
        digest.setPassword("somePassword");

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -1);

        identityManager.updateCredential(user, digest, new Date(), expirationDate.getTime());

        DigestCredentials credential = new DigestCredentials(digest);

        digest.setDigest(DigestUtil.calculateA1(user.getLoginName(), digest.getRealm(), digest.getPassword().toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());

        digest.setDigest(DigestUtil.calculateA1(user.getLoginName(), digest.getRealm(), "bad_password".toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Status.INVALID, credential.getStatus());

        Digest newPassword = new Digest();

        newPassword.setRealm("pl-idm");
        newPassword.setUsername(user.getLoginName());
        newPassword.setPassword("someNewPassword");

        identityManager.updateCredential(user, newPassword);

        credential = new DigestCredentials(newPassword);

        newPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), newPassword.getRealm(), newPassword.getPassword().toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }
    
    @Test
    public void testMultipleRealms() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest realmAPassword = new Digest();

        realmAPassword.setRealm("Realm A");
        realmAPassword.setUsername(user.getLoginName());
        realmAPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, realmAPassword);

        Digest realmBPassword = new Digest();

        realmBPassword.setRealm("Realm B");
        realmBPassword.setUsername(user.getLoginName());
        realmBPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, realmBPassword);

        realmAPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), realmAPassword.getRealm(), realmAPassword.getPassword().toCharArray()));
        
        DigestCredentials realmACredentials = new DigestCredentials(realmAPassword);

        identityManager.validateCredentials(realmACredentials);

        assertEquals(Status.VALID, realmACredentials.getStatus());
        
        realmBPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), realmBPassword.getRealm(), realmBPassword.getPassword().toCharArray()));
        
        DigestCredentials realmBCredentials = new DigestCredentials(realmBPassword);

        identityManager.validateCredentials(realmBCredentials);

        assertEquals(Status.VALID, realmBCredentials.getStatus());

        realmBPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), realmAPassword.getRealm(), realmBPassword.getPassword().toCharArray()));
        
        realmBCredentials = new DigestCredentials(realmBPassword);

        identityManager.validateCredentials(realmBCredentials);

        assertEquals(Status.INVALID, realmBCredentials.getStatus());
    }
    
    @Test
    public void testUserDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(user.getLoginName());
        digestPassword.setPassword("somePassword");
        
        identityManager.updateCredential(user, digestPassword);
        
        digestPassword.setDigest(DigestUtil.calculateA1(user.getLoginName(), digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));
        
        DigestCredentials credential = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        
        user.setEnabled(false);
        
        identityManager.update(user);
        
        identityManager.validateCredentials(credential);
        
        assertEquals(Status.ACCOUNT_DISABLED, credential.getStatus());
    }

    @Test
    @Configuration (exclude = LDAPStoreConfigurationTester.class)
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Digest realmAPassword = new Digest();

        realmAPassword.setRealm("Realm A");
        realmAPassword.setUsername(user.getLoginName());
        realmAPassword.setPassword("somePassword");

        identityManager.updateCredential(user, realmAPassword);

        DigestCredentialStorage currentStorage = identityManager.retrieveCurrentCredential(user, DigestCredentialStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getHa1());
        assertNotNull(currentStorage.getRealm());
    }
}