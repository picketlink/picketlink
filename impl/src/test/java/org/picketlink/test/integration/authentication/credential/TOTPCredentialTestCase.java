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
package org.picketlink.test.integration.authentication.credential;

import java.util.Calendar;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.internal.DigestUtil;
import org.picketlink.idm.credential.totp.TimeBasedOTP;
import org.picketlink.idm.model.Realm;
import org.picketlink.test.integration.ArchiveUtils;
import org.picketlink.test.integration.authentication.AbstractAuthenticationTestCase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author pedroigor
 */
public class TOTPCredentialTestCase extends AbstractAuthenticationTestCase {

    public static final String USER_TOTP_SECRET = "my_secret";
    public static final String USER_PASSWORD = "passwd";

    @Deployment
    public static WebArchive createDeployment() {
        return ArchiveUtils.create(TOTPCredentialTestCase.class);
    }

    @Test
    public void testSuccessfullAuthentication() {
        updateTOTPCredential();

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(USER_NAME);
        credentials.setPassword(new Password(USER_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        credentials.setToken(totp.generate(USER_TOTP_SECRET));

        super.credentials.setCredential(credentials);
        super.identity.login();

        assertTrue(super.identity.isLoggedIn());
    }

    @Test
    public void testUnsuccessfullAuthentication() {
        updateTOTPCredential();

        TOTPCredentials credentials = new TOTPCredentials();

        credentials.setUsername(USER_NAME);
        credentials.setPassword(new Password(USER_PASSWORD));

        TimeBasedOTP totp = new TimeBasedOTP();

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.SECOND, -60);

        totp.setCalendar(calendar);

        credentials.setToken(totp.generate(USER_TOTP_SECRET));

        super.credentials.setCredential(credentials);
        super.identity.login();

        assertFalse(super.identity.isLoggedIn());
    }

    private void updateTOTPCredential() {
        TOTPCredential credential = new TOTPCredential(USER_PASSWORD, USER_TOTP_SECRET);

        super.identityManager.updateCredential(super.identityManager.getUser(USER_NAME), credential);
    }

}
