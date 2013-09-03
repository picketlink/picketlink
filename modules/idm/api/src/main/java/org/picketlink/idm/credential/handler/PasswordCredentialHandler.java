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

import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.encoder.PasswordEncoder;
import org.picketlink.idm.credential.encoder.SHAPasswordEncoder;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.random.AutoReseedSecureRandomProvider;
import org.picketlink.idm.credential.random.SecureRandomProvider;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;

import static org.picketlink.idm.IDMLogger.*;

/**
 * <p> This particular implementation supports the validation of {@link UsernamePasswordCredentials}, and updating
 * {@link Password} credentials. </p> <p> <p/> <p> How passwords are encoded can be changed by specifying a
 * configuration option using the <code>PASSWORD_ENCODER</code>. By default a SHA-512 encoding is performed. </p> <p/>
 * <p> Password are always salted before encoding. </p>
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({UsernamePasswordCredentials.class, Password.class})
public class PasswordCredentialHandler<S extends CredentialStore<?>, V extends UsernamePasswordCredentials, U extends Password>
        extends AbstractCredentialHandler<S, V, U> {

    /**
     * <p> Stores a <b>stateless</b> instance of {@link PasswordEncoder} that should be used to encode passwords. </p>
     */
    public static final String PASSWORD_ENCODER = "PASSWORD_ENCODER";

    /**
     * <p> Stores an instance of {@link SecureRandomProvider} that should be used to obtain instances of {@link SecureRandom}</p>
     */
    public static final String SECURE_RANDOM_PROVIDER = "SECURE_RANDOM_PROVIDER";

    private PasswordEncoder passwordEncoder = new SHAPasswordEncoder(512);

    private SecureRandomProvider secureRandomProvider;

    @Override
    public void setup(S store) {
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

            Object secureRandomProvider = options.get(SECURE_RANDOM_PROVIDER);

            if (secureRandomProvider != null) {
                if (SecureRandomProvider.class.isInstance(secureRandomProvider)) {
                    this.secureRandomProvider = (SecureRandomProvider) secureRandomProvider;
                } else {
                    throw new SecurityConfigurationException("The secure random provider [" + secureRandomProvider
                            + "] must be an instance of " + SecureRandomProvider.class.getName());
                }
            } else {
                AutoReseedSecureRandomProvider autoReseedSecureRandomProvider = new AutoReseedSecureRandomProvider();
                autoReseedSecureRandomProvider.start();
                this.secureRandomProvider = autoReseedSecureRandomProvider;

                // put it back to properties, so that other CredentialHandler can reuse this
                options.put(SECURE_RANDOM_PROVIDER, this.secureRandomProvider);
            }
            LOGGER.usedSecureRandomProvider(this.secureRandomProvider.toString());
        }
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
        SecureRandom pseudoRandom = secureRandomProvider.getSecureRandom();
        return String.valueOf(pseudoRandom.nextLong());
    }

}
