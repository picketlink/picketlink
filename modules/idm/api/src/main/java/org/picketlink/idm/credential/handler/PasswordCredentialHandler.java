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

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.picketlink.common.random.DefaultSecureRandomProvider;
import org.picketlink.common.random.SecureRandomProvider;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.encoder.PasswordEncoder;
import org.picketlink.idm.credential.encoder.SHAPasswordEncoder;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

/**
 * <p> This particular implementation supports the validation of {@link UsernamePasswordCredentials}, and updating
 * {@link Password} credentials. </p> <p> <p/> <p> How passwords are encoded can be changed by specifying a
 * configuration option using the <code>PASSWORD_ENCODER</code>. By default a SHA-512 encoding is performed. </p> <p/>
 * <p> Password are always salted before encoding. </p>
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials(
        credentialClass = {UsernamePasswordCredentials.class, Password.class},
        credentialStorage = EncodedPasswordStorage.class)
public class PasswordCredentialHandler<S extends CredentialStore<?>, V extends UsernamePasswordCredentials, U extends Password>
        extends AbstractCredentialHandler<S, V, U> {

    private static final String DEFAULT_SALT_ALGORITHM = "SHA1PRNG";

    /**
     * <p> Stores a <b>stateless</b> instance of {@link PasswordEncoder} that should be used to encode passwords. </p>
     */
    public static final String PASSWORD_ENCODER = "PASSWORD_ENCODER";

    /**
     * <p>An user-defined {@link SecureRandomProvider} instance.</p>
     */
    public static final String SECURE_RANDOM_PROVIDER = "SECURE_RANDOM_PROVIDER";

    /**
     * <p>Time interval to be used to get a fresh {@link SecureRandom} instance.</p>
     */
    public static final String RENEW_RANDOM_NUMBER_GENERATOR_INTERVAL = "RENEW_RANDOM_NUMBER_GENERATOR_INTERVAL";

    /**
     * <p>The algorithm to be used to salt passwords.</p>
     */
    public static final String ALGORITHM_RANDOM_NUMBER = "ALGORITHM_RANDOM_NUMBER";

    /**
     * <p>Key length when generating a seed for random numbers.</p>
     */
    public static final String KEY_LENGTH_RANDOM_NUMBER = "KEY_LENGTH_RANDOM_NUMBER";

    private PasswordEncoder passwordEncoder = new SHAPasswordEncoder(512);

    private final Lock lock = new ReentrantLock();
    private Integer renewRandomNumberGeneratorInterval = -1;
    private AtomicLong lastRenewTime = new AtomicLong();

    private SecureRandomProvider secureRandomProvider;
    private SecureRandom secureRandom;

    @Override
    public void setup(S store) {
        super.setup(store);

        Map<String, Object> options = store.getConfig().getCredentialHandlerProperties();

        if (options != null) {
            Object providedEncoder = options.get(PASSWORD_ENCODER);

            if (providedEncoder != null) {
                if (PasswordEncoder.class.isInstance(providedEncoder)) {
                    this.passwordEncoder = (PasswordEncoder) providedEncoder;
                } else {
                    throw new SecurityConfigurationException("The password encoder [" + providedEncoder
                            + "] must be an instance of " + PasswordEncoder.class.getName());
                }
            }

            Object renewRandomNumberGeneratorInterval = options.get(RENEW_RANDOM_NUMBER_GENERATOR_INTERVAL);

            if (renewRandomNumberGeneratorInterval != null) {
                this.renewRandomNumberGeneratorInterval = Integer.valueOf(renewRandomNumberGeneratorInterval.toString());
            }

            Object secureRandomProvider = options.get(SECURE_RANDOM_PROVIDER);

            if (secureRandomProvider != null) {
                this.secureRandomProvider = (SecureRandomProvider) secureRandomProvider;
            } else {
                Object saltAlgorithm = options.get(ALGORITHM_RANDOM_NUMBER);

                if (saltAlgorithm == null) {
                    saltAlgorithm = DEFAULT_SALT_ALGORITHM;
                }

                Object keyLengthRandomNumber = options.get(KEY_LENGTH_RANDOM_NUMBER);

                if (keyLengthRandomNumber == null) {
                    keyLengthRandomNumber = Integer.valueOf(0);
                }

                this.secureRandomProvider = new DefaultSecureRandomProvider(saltAlgorithm.toString(), Integer.valueOf(keyLengthRandomNumber.toString()));
            }
        }

        this.secureRandom = createSecureRandom();
    }

    @Override
    protected Account getAccount(final IdentityContext context, final V credentials) {
        return getAccount(context, credentials.getUsername());
    }

    @Override
    protected CredentialStorage getCredentialStorage(final IdentityContext context, final Account account, final V credentials, final S store) {
        return store.retrieveCurrentCredential(context, account, EncodedPasswordStorage.class);
    }

    @Override
    protected boolean validateCredential(final CredentialStorage storage, final V credentials) {
        EncodedPasswordStorage hash = (EncodedPasswordStorage) storage;

        if (hash != null) {
            String rawPassword = new String(credentials.getPassword().getValue());
            return this.passwordEncoder.verify(saltPassword(rawPassword, hash.getSalt()), hash.getEncodedHash());
        }

        return false;
    }

    @Override
    public void update(IdentityContext context, Account account, U password, S store,
                       Date effectiveDate, Date expiryDate) {

        EncodedPasswordStorage hash = new EncodedPasswordStorage();

        if (password.getValue() == null || isNullOrEmpty(password.getValue().toString())) {
            throw MESSAGES.credentialInvalidPassword();
        }

        String rawPassword = new String(password.getValue());

        String passwordSalt = generateSalt();

        hash.setSalt(passwordSalt);
        hash.setEncodedHash(this.passwordEncoder.encode(saltPassword(rawPassword, passwordSalt)));

        if (effectiveDate != null) {
            hash.setEffectiveDate(effectiveDate);
        }

        hash.setExpiryDate(expiryDate);

        store.storeCredential(context, account, hash);
    }

    protected SecureRandomProvider getSecureRandomProvider() {
        return this.secureRandomProvider;
    }

    /**
     * <p> Salt the give <code>rawPassword</code> with the specified <code>salt</code> value. </p>
     *
     * @param rawPassword
     * @param salt
     * @return
     */
    private String saltPassword(String rawPassword, String salt) {
        return salt + rawPassword;
    }

    /**
     * <p> Generates a random string to be used as a salt for passwords. </p>
     *
     * @return
     */
    private String generateSalt() {
        return String.valueOf(getSecureRandom().nextLong());
    }

    private void renewSecureRandom() {
        if (isSecureRandomOutDated()) {
            if (this.lock.tryLock()) {
                try {
                    this.lastRenewTime.set(new Date().getTime());
                    this.secureRandom = createSecureRandom();
                } finally {
                    this.lock.unlock();
                }
            }
        }
    }

    private SecureRandom createSecureRandom() {
        try {
            return getSecureRandomProvider().getSecureRandom();
        } catch (Exception e) {
            throw new IdentityManagementException("Error getting SecureRandom instance from provider [" + this.secureRandomProvider + "].", e);
        }
    }

    private SecureRandom getSecureRandom() {
        renewSecureRandom();
        return this.secureRandom;
    }

    private boolean isSecureRandomOutDated() {
        if (this.renewRandomNumberGeneratorInterval == -1) {
            return false;
        }

        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date(this.lastRenewTime.get()));
        calendar.add(Calendar.MILLISECOND, this.renewRandomNumberGeneratorInterval);

        return calendar.getTime().compareTo(new Date()) <= 0;
    }

}