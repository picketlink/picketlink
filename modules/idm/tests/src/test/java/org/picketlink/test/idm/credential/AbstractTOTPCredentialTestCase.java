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
import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.storage.OTPCredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.credential.util.TimeBasedOTP;
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
public abstract class AbstractTOTPCredentialTestCase extends AbstractPartitionManagerTestCase {

    public static final String DEFAULT_TOTP_SECRET = "my_secret";
    public static final String DEFAULT_PASSWORD = "passwd";

    public AbstractTOTPCredentialTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(accountName);
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        String token = totp.generate(DEFAULT_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());
        assertNotNull(credentials.getValidatedAccount());
    }

    @Test
    public void testSuccessfulNullDevice() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
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

        credentials.setUsername(accountName);
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        // validate default device credentials
        credentials.setToken(totp.generate(DEFAULT_TOTP_SECRET));
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        // validate iphone device credentials
        credentials.setToken(totp.generate(iphoneSecret));
        credentials.setDevice(null);
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        // validate iphone device credentials
        credentials.setToken(totp.generate(androidSecret));
        credentials.setDevice(null);
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        credentials.setToken(totp.generate("bad_secret"));
        credentials.setDevice(null);
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.INVALID, credentials.getStatus());
    }

    @Test
    public void testMultipleDevices() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
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

        credentials.setUsername(accountName);
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        // validate default device credentials
        credentials.setToken(totp.generate(DEFAULT_TOTP_SECRET));
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        // validate iphone device credentials
        credentials.setToken(totp.generate(iphoneSecret));
        credentials.setDevice(iphoneDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        // validate android device credentials
        credentials.setToken(totp.generate(androidSecret));
        credentials.setDevice(androidDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());

        // should fail, trying to use a iphone token in a android device
        credentials.setToken(totp.generate(iphoneSecret));
        credentials.setDevice(androidDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.INVALID, credentials.getStatus());
        assertNull(credentials.getValidatedAccount());

        // should fail, trying to use a android token in a iphone device
        credentials.setToken(totp.generate(androidSecret));
        credentials.setDevice(iphoneDeviceName);
        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.INVALID, credentials.getStatus());
        assertNull(credentials.getValidatedAccount());
    }

    @Test
    public void testDelayWindow() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(accountName);
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -30);

        totp.setCalendar(calendar);

        String token = totp.generate("my_secret");

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.VALID, credentials.getStatus());
    }

    @Test
    public void testUpdatePasswordAndSecret() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials validatingCredential = new TOTPCredentials();

        validatingCredential.setUsername(accountName);
        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Credentials.Status.VALID, validatingCredential.getStatus());

        credential = new TOTPCredential("new_password", DEFAULT_TOTP_SECRET);

        Thread.sleep(1000);

        // update only the password
        identityManager.updateCredential(user, credential);

        validatingCredential.setPassword(new Password("new_password"));
        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Credentials.Status.VALID, validatingCredential.getStatus());

        credential = new TOTPCredential("new_password", "new_secret");

        Thread.sleep(1000);

        // now we update only the secret
        identityManager.updateCredential(user, credential);

        validatingCredential.setPassword(new Password("new_password"));
        validatingCredential.setToken(totp.generate("new_secret"));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Credentials.Status.VALID, validatingCredential.getStatus());

        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));
        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Credentials.Status.INVALID, validatingCredential.getStatus());
        assertNull(validatingCredential.getValidatedAccount());
    }

    @Test
    public void testUpdateSecret() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials validatingCredential = new TOTPCredentials();

        validatingCredential.setUsername(accountName);
        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        validatingCredential.setToken(totp.generate(DEFAULT_TOTP_SECRET));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Credentials.Status.VALID, validatingCredential.getStatus());

        credential = new TOTPCredential("new_secret");

        // update only the secret
        identityManager.updateCredential(user, credential);

        validatingCredential.setPassword(new Password(DEFAULT_PASSWORD));
        validatingCredential.setToken(totp.generate("new_secret"));

        identityManager.validateCredentials(validatingCredential);

        assertEquals(Credentials.Status.VALID, validatingCredential.getStatus());
    }

    @Test
    public void testInvalidToken() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(accountName);
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -60);

        totp.setCalendar(calendar);

        String token = totp.generate("my_secret");

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.INVALID, credentials.getStatus());
        assertNull(credentials.getValidatedAccount());
    }

    @Test
    public void testResetPassword() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
        TOTPCredential credential = new TOTPCredential(DEFAULT_PASSWORD, DEFAULT_TOTP_SECRET);

        Calendar expirationDate = Calendar.getInstance();

        expirationDate.add(Calendar.MINUTE, -5);

        identityManager.updateCredential(user, credential, new Date(), expirationDate.getTime());

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(accountName);
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        String token = totp.generate(DEFAULT_TOTP_SECRET);

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.EXPIRED, credentials.getStatus());

        credentials.setUsername(accountName);
        credentials.setPassword(new Password(DEFAULT_PASSWORD));

        credentials.setToken("12345678");

        identityManager.validateCredentials(credentials);

        assertEquals(Credentials.Status.INVALID, credentials.getStatus());
    }

    @Test
    @Configuration(exclude = LDAPStoreConfigurationTester.class)
    public void testRetrieveCurrentCredential() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        String accountName = "someUser";
        Account user = createAccount(accountName);
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

    protected abstract Account createAccount(String accountName);
}
