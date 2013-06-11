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

package org.picketlink.idm.credential.internal;

import java.util.Date;
import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.credential.totp.TimeBasedOTP;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.SecurityContext;
import static org.picketlink.idm.credential.Credentials.Status;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_ALGORITHM;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_DELAY_WINDOW;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_INTERVAL_SECONDS;
import static org.picketlink.idm.credential.totp.TimeBasedOTP.DEFAULT_NUMBER_DIGITS;

/**
 * <p>
 * This particular implementation supports the validation of {@link org.picketlink.idm.credential.UsernamePasswordCredentials}, and updating {@link org.picketlink.idm.credential.Password}
 * credentials.
 * </p>
 * <p>
 * <p/>
 * <p>
 * How passwords are encoded can be changed by specifying a configuration option using the <code>PASSWORD_ENCODER</code>. By
 * default a SHA-512 encoding is performed.
 * </p>
 * <p/>
 * <p>
 * Password are always salted before encoding.
 * </p>
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({TOTPCredentials.class, TOTPCredential.class})
public class TOTPCredentialHandler extends PasswordCredentialHandler<CredentialStore<?>, TOTPCredentials, TOTPCredential> {

    public static final String ALGORITHM = "ALGORITHM";
    public static final String INTERVAL_SECONDS = "INTERVAL_SECONDS";
    public static final String NUMBER_DIGITS = "NUMBER_DIGITS";
    public static final String DELAY_WINDOW = "DELAY_WINDOW";

    private TimeBasedOTP totp;

    @Override
    public void setup(CredentialStore<?> store) {
        super.setup(store);

        String algorithm = getConfigurationProperty(store, ALGORITHM, DEFAULT_ALGORITHM);
        String intervalSeconds = getConfigurationProperty(store, INTERVAL_SECONDS, "" + DEFAULT_INTERVAL_SECONDS);
        String numberDigits = getConfigurationProperty(store, NUMBER_DIGITS, "" + DEFAULT_NUMBER_DIGITS);
        String delayWindow = getConfigurationProperty(store, DELAY_WINDOW, "" + DEFAULT_DELAY_WINDOW);

        this.totp = new TimeBasedOTP(algorithm, Integer.parseInt(numberDigits), Integer.valueOf(intervalSeconds), Integer.valueOf(delayWindow));
    }

    @Override
    public void validate(SecurityContext context, TOTPCredentials credentials, CredentialStore<?> store) {
        super.validate(context, credentials, store);

        // password is valid, let's validate the token now
        if (Status.VALID.equals(credentials.getStatus())) {
            Agent agent = credentials.getValidatedAgent();

            OTPCredentialStorage storage = store.retrieveCurrentCredential(context, agent, OTPCredentialStorage.class);

            if (storage != null) {
                String secretKey = storage.getSecretKey();
                String token = credentials.getToken();

                if (this.totp.validate(token, secretKey.getBytes())) {
                    credentials.setStatus(Status.INVALID);
                    credentials.setValidatedAgent(null);
                }
            }
        }
    }

    @Override
    public void update(SecurityContext context, Agent agent, TOTPCredential password, CredentialStore<?> store, Date effectiveDate, Date expiryDate) {
        // if a password was not provided, updates only the secret.
        if (password.getValue() != null) {
            super.update(context, agent, password, store, effectiveDate, expiryDate);
        }

        OTPCredentialStorage storage = new OTPCredentialStorage();

        storage.setEffectiveDate(effectiveDate);
        storage.setExpiryDate(expiryDate);

        storage.setSecretKey(password.getSecret());

        store.storeCredential(context, agent, storage);
    }

    private String getConfigurationProperty(CredentialStore<?> store, String key, String defaultValue) {
        Object algorithm = store.getConfig().getCredentialHandlerProperties().get(key);

        if (algorithm != null) {
            return String.valueOf(algorithm);
        }

        return defaultValue;
    }

}