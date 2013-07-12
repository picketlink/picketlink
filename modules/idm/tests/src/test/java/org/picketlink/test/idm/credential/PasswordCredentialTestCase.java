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

import java.util.Calendar;
import java.util.Date;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * <p>
 * Test case for {@link UsernamePasswordCredentials} type.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
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
        assertNotNull(credential.getValidatedAgent());
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
        assertNull(badUserName.getValidatedAgent());

        UsernamePasswordCredentials badPassword = new UsernamePasswordCredentials();

        plainTextPassword = new Password("bad_password".toCharArray());

        badPassword.setUsername(user.getLoginName());
        badPassword.setPassword(plainTextPassword);

        identityManager.validateCredentials(badPassword);

        assertEquals(Status.INVALID, badPassword.getStatus());
        assertNull(badPassword.getValidatedAgent());

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
    @IgnoreTester (LDAPStoreConfigurationTester.class)
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
    @IgnoreTester(LDAPStoreConfigurationTester.class)
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

        assertEquals(Status.AGENT_DISABLED, credential.getStatus());
    }
}