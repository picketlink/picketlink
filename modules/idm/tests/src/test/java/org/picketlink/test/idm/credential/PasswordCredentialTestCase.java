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
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.basic.CustomAccountTestCase;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for {@link UsernamePasswordCredentials} type.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@Configuration(include = {SingleConfigLDAPJPAStoreConfigurationTester.class, JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
public class PasswordCredentialTestCase extends AbstractPartitionManagerTestCase {

    public PasswordCredentialTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(user.getLoginName());
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }

    @Test
    public void testUnsuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword, new Date(), null);
        UsernamePasswordCredentials badUserName = new UsernamePasswordCredentials();

        badUserName.setUsername("Bad" + user.getLoginName());
        badUserName.setPassword(plainTextPassword);

        identityManager.validateCredentials(badUserName);

        assertEquals(Status.INVALID, badUserName.getStatus());
        assertNull(badUserName.getValidatedAccount());

        UsernamePasswordCredentials badPassword = new UsernamePasswordCredentials();

        plainTextPassword = new Password("bad_password".toCharArray());

        badPassword.setUsername(user.getLoginName());
        badPassword.setPassword(plainTextPassword);

        identityManager.validateCredentials(badPassword);

        assertEquals(Status.INVALID, badPassword.getStatus());
        assertNull(badPassword.getValidatedAccount());

    }

    @Test
    public void testEmptyPasswordValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        String emptyPassword = null;
        Password plainTextPassword = new Password(emptyPassword);

        identityManager.updateCredential(user, plainTextPassword, new Date(), null);
        UsernamePasswordCredentials badUserName = new UsernamePasswordCredentials();

        badUserName.setUsername("Bad" + user.getLoginName());
        badUserName.setPassword(plainTextPassword);

        identityManager.validateCredentials(badUserName);

        assertEquals(Status.INVALID, badUserName.getStatus());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testExpiration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        identityManager.updateCredential(user, plainTextPassword, new Date(), expirationDate.getTime());
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(user.getLoginName());
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());

        Password newPassword = new Password("new_password".toCharArray());

        Thread.sleep(1000);

        identityManager.updateCredential(user, newPassword);

        credential = new UsernamePasswordCredentials(user.getLoginName(), newPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testResetPassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        identityManager.updateCredential(user, plainTextPassword, new Date(), expirationDate.getTime());
        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(user.getLoginName());
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.EXPIRED, credential.getStatus());

        credential.setUsername(user.getLoginName());
        credential.setPassword(new Password("bad_password"));

        identityManager.validateCredentials(credential);

        assertEquals(Status.INVALID, credential.getStatus());

        Password newPassword = new Password("new_password".toCharArray());

        identityManager.updateCredential(user, newPassword);

        credential = new UsernamePasswordCredentials(user.getLoginName(), newPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
    }

    @Test
    public void testUpdatePassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password firstPassword = new Password("password1".toCharArray());

        identityManager.updateCredential(user, firstPassword);

        UsernamePasswordCredentials firstCredential = new UsernamePasswordCredentials(user.getLoginName(), firstPassword);

        identityManager.validateCredentials(firstCredential);

        assertEquals(Status.VALID, firstCredential.getStatus());

        Password secondPassword = new Password("password2".toCharArray());

        Thread.sleep(1000);

        identityManager.updateCredential(user, secondPassword);

        UsernamePasswordCredentials secondCredential = new UsernamePasswordCredentials(user.getLoginName(), secondPassword);

        identityManager.validateCredentials(secondCredential);

        assertEquals(Status.VALID, secondCredential.getStatus());

        identityManager.validateCredentials(firstCredential);

        assertEquals(Status.INVALID, firstCredential.getStatus());
    }

    @Test
    public void testUserDeletion() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User john = createUser("john");
        Password johnPassword = new Password("123".toCharArray());

        identityManager.updateCredential(john, johnPassword);

        UsernamePasswordCredentials johnCredential = new UsernamePasswordCredentials(john.getLoginName(), johnPassword);

        identityManager.validateCredentials(johnCredential);

        assertEquals(Status.VALID, johnCredential.getStatus());

        User francesco = createUser("francesco");
        Password francescoPassword = new Password("123".toCharArray());

        identityManager.updateCredential(francesco, francescoPassword);

        UsernamePasswordCredentials francescoCredential = new UsernamePasswordCredentials(francesco.getLoginName(),
                francescoPassword);

        identityManager.validateCredentials(francescoCredential);

        assertEquals(Status.VALID, francescoCredential.getStatus());

        identityManager.remove(francesco);

        identityManager.validateCredentials(johnCredential);
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testUserDisabled() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(user.getLoginName());
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());

        user.setEnabled(false);

        identityManager.update(user);

        identityManager.validateCredentials(credential);

        assertEquals(Status.ACCOUNT_DISABLED, credential.getStatus());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testRandomSaltGeneration() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);
        identityManager.updateCredential(user, plainTextPassword);
        identityManager.updateCredential(user, plainTextPassword);
        identityManager.updateCredential(user, plainTextPassword);
        identityManager.updateCredential(user, plainTextPassword);
        identityManager.updateCredential(user, plainTextPassword);

        List<EncodedPasswordStorage> storages = identityManager.retrieveCredentials(user, EncodedPasswordStorage.class);

        assertFalse(storages.isEmpty());
        assertEquals(6, storages.size());

        Set<String> salts = new HashSet<String>();

        for (EncodedPasswordStorage storage: storages) {
            assertTrue(salts.add(storage.getSalt()));
        }
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        Password plainTextPassword = new Password("updated_password".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        EncodedPasswordStorage currentStorage = identityManager.retrieveCurrentCredential(user, EncodedPasswordStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getEncodedHash());
        assertNotNull(currentStorage.getSalt());
    }

    @Test
    @Configuration(exclude = {LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testCustomAccountAuthentication() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        CustomAccountTestCase.MyCustomAccount user = new CustomAccountTestCase.MyCustomAccount("customAccount");

        identityManager.add(user);

        Password plainTextPassword = new Password("abcd1234".toCharArray());

        identityManager.updateCredential(user, plainTextPassword);

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();

        credential.setUsername(user.getUserName());
        credential.setPassword(plainTextPassword);

        identityManager.validateCredentials(credential);

        assertEquals(Status.VALID, credential.getStatus());
        assertNotNull(credential.getValidatedAccount());
    }
}