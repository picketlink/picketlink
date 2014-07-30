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
package org.picketlink.test.idm.credential;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.storage.DigestCredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.credential.util.DigestUtil;
import org.picketlink.idm.model.Account;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public abstract class AbstractDigestCredentialTestCase extends AbstractPartitionManagerTestCase {

    public AbstractDigestCredentialTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(accountName);
        digestPassword.setPassword("somePassword");

        identityManager.updateCredential(user, digestPassword);

        digestPassword.setDigest(DigestUtil
            .calculateA1(accountName, digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));

        DigestCredentials credential = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
        assertEquals(user.getId(), credential.getValidatedAccount().getId());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(accountName);
        digestPassword.setPassword("somePassword");

        identityManager.updateCredential(user, digestPassword);

        digestPassword.setDigest(DigestUtil.calculateA1("Bad" + accountName, digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));

        DigestCredentials badUserName = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(badUserName);

        assertEquals(Credentials.Status.INVALID, badUserName.getStatus());
        assertNull(badUserName.getValidatedAccount());

        digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(accountName);
        digestPassword.setPassword("bad_somePassword");

        digestPassword.setDigest(DigestUtil.calculateA1(accountName, digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));

        DigestCredentials badPassword = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(badPassword);

        assertEquals(Credentials.Status.INVALID, badPassword.getStatus());
        assertNull(badPassword.getValidatedAccount());

    }

    @Test
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Digest digest = new Digest();

        digest.setRealm("pl-idm");
        digest.setUsername(accountName);
        digest.setPassword("somePassword");

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -1);

        identityManager.updateCredential(user, digest, new Date(), expirationDate.getTime());

        DigestCredentials credential = new DigestCredentials(digest);

        digest.setDigest(DigestUtil.calculateA1(accountName, digest.getRealm(), digest.getPassword().toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.EXPIRED, credential.getStatus());

        Digest newPassword = new Digest();

        newPassword.setRealm("pl-idm");
        newPassword.setUsername(accountName);
        newPassword.setPassword("someNewPassword");

        identityManager.updateCredential(user, newPassword);

        credential = new DigestCredentials(newPassword);

        newPassword.setDigest(DigestUtil.calculateA1(accountName, newPassword.getRealm(), newPassword.getPassword().toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
    }

    @Test
    public void testResetCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Digest digest = new Digest();

        digest.setRealm("pl-idm");
        digest.setUsername(accountName);
        digest.setPassword("somePassword");

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -1);

        identityManager.updateCredential(user, digest, new Date(), expirationDate.getTime());

        DigestCredentials credential = new DigestCredentials(digest);

        digest.setDigest(DigestUtil.calculateA1(accountName, digest.getRealm(), digest.getPassword().toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.EXPIRED, credential.getStatus());

        digest.setDigest(DigestUtil.calculateA1(accountName, digest.getRealm(), "bad_password".toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.INVALID, credential.getStatus());

        Digest newPassword = new Digest();

        newPassword.setRealm("pl-idm");
        newPassword.setUsername(accountName);
        newPassword.setPassword("someNewPassword");

        identityManager.updateCredential(user, newPassword);

        credential = new DigestCredentials(newPassword);

        newPassword.setDigest(DigestUtil.calculateA1(accountName, newPassword.getRealm(), newPassword.getPassword().toCharArray()));

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
    }

    @Test
    public void testUserDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Digest digestPassword = new Digest();

        digestPassword.setRealm("pl-idm");
        digestPassword.setUsername(accountName);
        digestPassword.setPassword("somePassword");

        identityManager.updateCredential(user, digestPassword);

        digestPassword.setDigest(DigestUtil.calculateA1(accountName, digestPassword.getRealm(), digestPassword.getPassword().toCharArray()));

        DigestCredentials credential = new DigestCredentials(digestPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());

        user.setEnabled(false);

        identityManager.update(user);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.ACCOUNT_DISABLED, credential.getStatus());
    }

    @Test
    @Configuration(exclude = LDAPStoreConfigurationTester.class)
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Digest realmAPassword = new Digest();

        realmAPassword.setRealm("Realm A");
        realmAPassword.setUsername(accountName);
        realmAPassword.setPassword("somePassword");

        identityManager.updateCredential(user, realmAPassword);

        DigestCredentialStorage currentStorage = identityManager.retrieveCurrentCredential(user, DigestCredentialStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getHa1());
        assertNotNull(currentStorage.getRealm());
    }

    protected abstract Account createAccount(String accountName);
}
