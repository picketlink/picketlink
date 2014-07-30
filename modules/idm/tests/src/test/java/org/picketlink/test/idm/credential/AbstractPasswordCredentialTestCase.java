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
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.model.Account;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public abstract class AbstractPasswordCredentialTestCase extends AbstractPartitionManagerTestCase {

    public AbstractPasswordCredentialTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(accountName);
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword, new Date(), null);
        UsernamePasswordCredentials badUserName = new UsernamePasswordCredentials();

        badUserName.setUsername("Bad" + accountName);
        badUserName.setPassword(plainTextPassword);

        identityManager.validateCredentials(badUserName);

        assertEquals(Credentials.Status.INVALID, badUserName.getStatus());
        assertNull(badUserName.getValidatedAccount());

        UsernamePasswordCredentials badPassword = new UsernamePasswordCredentials();

        plainTextPassword = new Password("bad_password".toCharArray());

        badPassword.setUsername(accountName);
        badPassword.setPassword(plainTextPassword);

        identityManager.validateCredentials(badPassword);

        assertEquals(Credentials.Status.INVALID, badPassword.getStatus());
        assertNull(badPassword.getValidatedAccount());

    }

    @Test
    public void testEmptyPasswordValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        String emptyPassword = null;
        Password plainTextPassword = new Password(emptyPassword);

        identityManager.updateCredential(user, plainTextPassword, new Date(), null);
        UsernamePasswordCredentials badUserName = new UsernamePasswordCredentials();

        badUserName.setUsername("Bad" + accountName);
        badUserName.setPassword(plainTextPassword);

        identityManager.validateCredentials(badUserName);

        assertEquals(Credentials.Status.INVALID, badUserName.getStatus());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Password plainTextPassword = new Password("updated_password".toCharArray());

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        identityManager.updateCredential(user, plainTextPassword, new Date(), expirationDate.getTime());
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(accountName);
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.EXPIRED, credential.getStatus());

        Password newPassword = new Password("new_password".toCharArray());

        Thread.sleep(1000);

        identityManager.updateCredential(user, newPassword);

        credential = new UsernamePasswordCredentials(accountName, newPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testResetPassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Password plainTextPassword = new Password("updated_password".toCharArray());

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        identityManager.updateCredential(user, plainTextPassword, new Date(), expirationDate.getTime());
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(accountName);
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.EXPIRED, credential.getStatus());

        credential.setUsername(accountName);
        credential.setPassword(new Password("bad_password"));

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.INVALID, credential.getStatus());

        Password newPassword = new Password("new_password".toCharArray());

        identityManager.updateCredential(user, newPassword);

        credential = new UsernamePasswordCredentials(accountName, newPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());
    }

    @Test
    public void testUpdatePassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Password firstPassword = new Password("password1".toCharArray());

        identityManager.updateCredential(user, firstPassword);

        UsernamePasswordCredentials firstCredential = new UsernamePasswordCredentials(accountName, firstPassword);

        identityManager.validateCredentials(firstCredential);

        assertEquals(Credentials.Status.VALID, firstCredential.getStatus());

        Password secondPassword = new Password("password2".toCharArray());

        Thread.sleep(1000);

        identityManager.updateCredential(user, secondPassword);

        UsernamePasswordCredentials secondCredential = new UsernamePasswordCredentials(accountName, secondPassword);

        identityManager.validateCredentials(secondCredential);

        assertEquals(Credentials.Status.VALID, secondCredential.getStatus());

        identityManager.validateCredentials(firstCredential);

        assertEquals(Credentials.Status.INVALID, firstCredential.getStatus());
    }

    @Test
    public void testUserDeletion() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String johnName = "john";
        Account john = createAccount(johnName);
        Password johnPassword = new Password("123".toCharArray());

        identityManager.updateCredential(john, johnPassword);

        UsernamePasswordCredentials johnCredential = new UsernamePasswordCredentials(johnName, johnPassword);

        identityManager.validateCredentials(johnCredential);

        assertEquals(Credentials.Status.VALID, johnCredential.getStatus());

        String francescoName = "francesco";
        Account francesco = createAccount(francescoName);
        Password francescoPassword = new Password("123".toCharArray());

        identityManager.updateCredential(francesco, francescoPassword);

        UsernamePasswordCredentials francescoCredential = new UsernamePasswordCredentials(francescoName, francescoPassword);

        identityManager.validateCredentials(francescoCredential);

        assertEquals(Credentials.Status.VALID, francescoCredential.getStatus());

        identityManager.remove(francesco);

        identityManager.validateCredentials(johnCredential);
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testUserDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(accountName);
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.VALID, credential.getStatus());

        user.setEnabled(false);

        identityManager.update(user);

        identityManager.validateCredentials(credential);

        assertEquals(Credentials.Status.ACCOUNT_DISABLED, credential.getStatus());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        EncodedPasswordStorage currentStorage = identityManager.retrieveCurrentCredential(user, EncodedPasswordStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getEncodedHash());
        assertNotNull(currentStorage.getSalt());
    }

    protected abstract Account createAccount(String accountName);
}
