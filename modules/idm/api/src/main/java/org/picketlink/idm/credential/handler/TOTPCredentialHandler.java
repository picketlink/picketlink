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

package org.picketlink.idm.credential.handler;

import org.picketlink.idm.credential.TOTPCredential;
import org.picketlink.idm.credential.TOTPCredentials;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.OTPCredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.credential.util.TimeBasedOTP;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.credential.Credentials.Status;
import static org.picketlink.idm.credential.util.TimeBasedOTP.DEFAULT_ALGORITHM;
import static org.picketlink.idm.credential.util.TimeBasedOTP.DEFAULT_DELAY_WINDOW;
import static org.picketlink.idm.credential.util.TimeBasedOTP.DEFAULT_INTERVAL_SECONDS;
import static org.picketlink.idm.credential.util.TimeBasedOTP.DEFAULT_NUMBER_DIGITS;

/**
 * <p> This particular implementation supports the validation of {@link TOTPCredentials}, and updating {@link
 * TOTPCredential} credentials. </p>
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials(
        credentialClass = {TOTPCredentials.class, TOTPCredential.class},
        credentialStorage = OTPCredentialStorage.class)
public class TOTPCredentialHandler extends PasswordCredentialHandler<CredentialStore<?>, TOTPCredentials, TOTPCredential> {

    public static final String ALGORITHM = "ALGORITHM";
    public static final String INTERVAL_SECONDS = "INTERVAL_SECONDS";
    public static final String NUMBER_DIGITS = "NUMBER_DIGITS";
    public static final String DELAY_WINDOW = "DELAY_WINDOW";
    public static final String DEFAULT_DEVICE = "DEFAULT_DEVICE";

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
    public void validate(final IdentityContext context, final TOTPCredentials credentials, final CredentialStore<?> store) {
        super.validate(context, credentials, store);

        if (Status.VALID.equals(credentials.getStatus()) || Status.EXPIRED.equals(credentials.getStatus())) {
            if (!isValid(context, credentials, store)) {
                credentials.setStatus(Status.INVALID);
                credentials.setValidatedAccount(null);
            }
        }
    }

    @Override
    public void update(IdentityContext context, Account account, TOTPCredential credential, CredentialStore<?> store, Date effectiveDate, Date expiryDate) {
        // if a credential was not provided, updates only the secret.
        if (credential.getValue() != null && credential.getValue().length > 0) {
            super.update(context, account, credential, store, effectiveDate, expiryDate);
        }

        OTPCredentialStorage storage = new OTPCredentialStorage();

        if (effectiveDate != null) {
            storage.setEffectiveDate(effectiveDate);
        }

        storage.setExpiryDate(expiryDate);

        storage.setSecretKey(credential.getSecret());
        storage.setDevice(getDevice(credential.getDevice()));

        store.storeCredential(context, account, storage);
    }

    private boolean isValid(final IdentityContext context, final TOTPCredentials credentials, final CredentialStore<?> store) {
        for (OTPCredentialStorage storage : getCredentialStorages(context, credentials, store)) {
            String secretKey = storage.getSecretKey();
            String token = credentials.getToken();

            if (this.totp.validate(token, secretKey.getBytes())) {
                return true;
            }
        }

        return false;
    }

    private List<OTPCredentialStorage> getCredentialStorages(final IdentityContext context, final TOTPCredentials credentials, final CredentialStore<?> store) {
        List<OTPCredentialStorage> storages = store.retrieveCredentials(context, getAccount(context,
                credentials.getUsername()), OTPCredentialStorage.class);

        for (OTPCredentialStorage storage : new ArrayList<OTPCredentialStorage>(storages)) {
            if (!CredentialUtils.isCurrentCredential(storage) || !isDeviceStorage(credentials.getDevice(), storage)) {
                storages.remove(storage);
            }
        }

        return storages;
    }

    /**
     * <p>Checks if the given {@link OTPCredentialStorage} references the given <code>device</code>. A null device
     * means that this storage can reference any device and this method will return true.</p>
     *
     * @param device
     * @param storage
     * @return
     */
    private boolean isDeviceStorage(String device, final OTPCredentialStorage storage) {
        return device == null || device.equals(storage.getDevice());
    }

    private String getDevice(String device) {
        if (isNullOrEmpty(device)) {
            device = DEFAULT_DEVICE;
        }

        return device;
    }

    private String getConfigurationProperty(CredentialStore<?> store, String key, String defaultValue) {
        Object value = store.getConfig().getCredentialHandlerProperties().get(key);

        if (value != null) {
            return String.valueOf(value);
        }

        return defaultValue;
    }

}