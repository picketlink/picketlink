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
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.storage.OTPCredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.credential.util.TimeBasedOTP;
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
import static org.picketlink.idm.credential.Credentials.*;

/**
 * <p>
 * Test case for {@link org.picketlink.idm.credential.DigestCredentials} type.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class TOTPCredentialTestCase extends AbstractPartitionManagerTestCase {

    public static final String DEFAULT_TOTP_SECRET = "my_secret";
    public static final String DEFAULT_PASSWORD = "passwd";

    public TOTPCredentialTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        String token = totp.generate(DEFAULT_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());
        assertNotNull(credentials.getValidatedAccount());
    }

    @Test
    public void testSuccessfulNullDevice() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential defaultDevice = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, defaultDevice);

        String iphoneSecret = "iphone_secret";
        TOTPCredential iphoneDevice = new TOTPCredential(DEFAULT_PASSWORD, iphoneSecret);

        String iphoneDeviceName = "My IPhone #SN-121212121";
        iphoneDevice.setDevice(iphoneDeviceName);
        identityManager.updateCredential(user, iphoneDevice);

        String androidSecret = "android_secret";
        TOTPCredential androidDevice = new TOTPCredential(DEFAULT_PASSWORD, androidSecret);

        String androidDeviceName = "My Android #SN-56757554";
        androidDevice.setDevice(androidDeviceName);
        identityManager.updateCredential(user, androidDevice);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        // validate default device credentials
        credentials.setToken(totp.generate(DEFAULT_TOTP_SECRET));
        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        // validate iphone device credentials
        credentials.setToken(totp.generate(iphoneSecret));
        credentials.setDevice(null);
        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        // validate iphone device credentials
        credentials.setToken(totp.generate(androidSecret));
        credentials.setDevice(null);
        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        credentials.setToken(totp.generate("bad_secret"));
        credentials.setDevice(null);
        identityManager.validateCredentials(credentials);

        assertEquals(Status.INVALID, credentials.getStatus());
    }

    @Test
    public void testMultipleDevices() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential defaultDevice = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, defaultDevice);

        String iphoneSecret = "iphone_secret";
        TOTPCredential iphoneDevice = new TOTPCredential(DEFAULT_PASSWORD, iphoneSecret);

        String iphoneDeviceName = "My IPhone #SN-121212121";
        iphoneDevice.setDevice(iphoneDeviceName);
        identityManager.updateCredential(user, iphoneDevice);

        String androidSecret = "android_secret";
        TOTPCredential androidDevice = new TOTPCredential(DEFAULT_PASSWORD, androidSecret);

        String androidDeviceName = "My Android #SN-56757554";
        androidDevice.setDevice(androidDeviceName);
        identityManager.updateCredential(user, androidDevice);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        // validate default device credentials
        credentials.setToken(totp.generate(DEFAULT_TOTP_SECRET));
        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        // validate iphone device credentials
        credentials.setToken(totp.generate(iphoneSecret));
        credentials.setDevice(iphoneDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        // validate android device credentials
        credentials.setToken(totp.generate(androidSecret));
        credentials.setDevice(androidDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());

        // should fail, trying to use a iphone token in a android device
        credentials.setToken(totp.generate(iphoneSecret));
        credentials.setDevice(androidDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Status.INVALID, credentials.getStatus());
        assertNull(credentials.getValidatedAccount());

        // should fail, trying to use a android token in a iphone device
        credentials.setToken(totp.generate(androidSecret));
        credentials.setDevice(iphoneDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Status.INVALID, credentials.getStatus());
        assertNull(credentials.getValidatedAccount());
    }

    @Test
    public void testDelayWindow() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -30);

        totp.setCalendar(calendar);

        String token = totp.generate("my_secret");

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.VALID, credentials.getStatus());
    }

    @Test
    public void testUpdatePasswordAndSecret() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials validatingCredential = new TOTPCredentials();

        validatingCredential.setUsername(user.getLoginName());
        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Status.VALID, validatingCredential.getStatus());

        credential = new TOTPCredential("new_password", DEFAULT_TOTP_SECRET);

        Thread.sleep(1000);

        // update only the password
        identityManager.updateCredential(user, credential);

        validatingCredential.setPassword(new Password("new_password"));
        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Status.VALID, validatingCredential.getStatus());

        credential = new TOTPCredential("new_password", "new_secret");

        Thread.sleep(1000);

        // now we update only the secret
        identityManager.updateCredential(user, credential);

        validatingCredential.setPassword(new Password("new_password"));
        validatingCredential.setToken(totp.generate("new_secret"));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Status.VALID, validatingCredential.getStatus());

        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));
        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Status.INVALID, validatingCredential.getStatus());
        assertNull(validatingCredential.getValidatedAccount());
    }

    @Test
    public void testUpdateSecret() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials validatingCredential = new TOTPCredentials();

        validatingCredential.setUsername(user.getLoginName());
        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Status.VALID, validatingCredential.getStatus());

        credential = new TOTPCredential("new_secret");

        // update only the secret
        identityManager.updateCredential(user, credential);

        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));
        validatingCredential.setToken(totp.generate("new_secret"));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Status.VALID, validatingCredential.getStatus());
    }

    @Test
    public void testInvalidToken() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -60);

        totp.setCalendar(calendar);

        String token = totp.generate("my_secret");

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.INVALID, credentials.getStatus());
        assertNull(credentials.getValidatedAccount());
    }

    @Test
    public void testResetPassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        identityManager.updateCredential(user, credential, new Date(), expirationDate.getTime());

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        String token = totp.generate(DEFAULT_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Status.EXPIRED, credentials.getStatus());

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        credentials.setToken("12345678");

        identityManager.validateCredentials(credentials);

        assertEquals(Status.INVALID, credentials.getStatus());
    }

    @Test
    @Configuration (exclude = LDAPStoreConfigurationTester.class)
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        credential.setDevice("My Android");

        identityManager.updateCredential(user, credential);

        OTPCredentialStorage currentStorage = identityManager.retrieveCurrentCredential(user, OTPCredentialStorage.class);

        assertNotNull(currentStorage);
        assertTrue(CredentialUtils.isCurrentCredential(currentStorage));

        assertNotNull(currentStorage.getEffectiveDate());
        assertNotNull(currentStorage.getDevice());
        assertNotNull(currentStorage.getSecretKey());
    }

}