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
import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.totp.TimeBasedOTP;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
import static org.picketlink.idm.credential.Credentials.Status;

/**
 * <p>
 * Test case for {@link org.picketlink.idm.credential.DigestCredentials} type.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class OTPCredentialTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testSuccessfulValidation() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential("passwd", "my_secret");

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password("passwd"));

        TimeBasedOTP totp = new TimeBasedOTP();

        String token = totp.generate("my_secret");

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        Assert.assertEquals(Status.VALID, credentials.getStatus());
    }

    @Test
    public void testDelayWindow() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential("passwd", "my_secret");

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password("passwd"));

        TimeBasedOTP totp = new TimeBasedOTP();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -30);

        totp.setCalendar(calendar);

        String token = totp.generate("my_secret");

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        Assert.assertEquals(Status.VALID, credentials.getStatus());
    }

    @Test
    public void testInvalidToken() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        User user = createUser("someUser");
        TOTPCredential credential = new TOTPCredential("passwd", "my_secret");

        identityManager.updateCredential(user, credential);

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(user.getLoginName());
        credentials.setPassword(new Password("passwd"));

        TimeBasedOTP totp = new TimeBasedOTP();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -60);

        totp.setCalendar(calendar);

        String token = totp.generate("my_secret");

        credentials.setToken(token);

        identityManager.validateCredentials(credentials);

        Assert.assertEquals(Status.INVALID, credentials.getStatus());
    }

}