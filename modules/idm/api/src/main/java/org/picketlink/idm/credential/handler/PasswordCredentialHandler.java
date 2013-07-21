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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.encoder.PasswordEncoder;
import org.picketlink.idm.credential.encoder.SHAPasswordEncoder;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.EncodedPasswordStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import static org.picketlink.idm.credential.util.CredentialUtils.isCredentialExpired;

/**
 * <p>
 * This particular implementation supports the validation of {@link UsernamePasswordCredentials}, and updating {@link Password}
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
@SupportsCredentials({UsernamePasswordCredentials.class, Password.class})
public class PasswordCredentialHandler<S extends CredentialStore<?>, V extends UsernamePasswordCredentials, U extends Password>
        extends AbstractCredentialHandler<S, V, U> {

    private static final String DEFAULT_SALT_ALGORITHM = "SHA1PRNG";

    /**
     * <p>
     * Stores a <b>stateless</b> instance of {@link PasswordEncoder} that should be used to encode passwords.
     * </p>
     */
    public static final String PASSWORD_ENCODER = "PASSWORD_ENCODER";

    private PasswordEncoder passwordEncoder = new SHAPasswordEncoder(512);

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
        }
    }

    @Override
    public void validate(IdentityContext context, V credentials, S store) {
        UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials) credentials;

        usernamePassword.setStatus(Status.INVALID);
        usernamePassword.setValidatedAgent(null);

        Agent agent = getAgent(context, usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            if (agent.isEnabled()) {
                EncodedPasswordStorage hash = store.retrieveCurrentCredential(context, agent, EncodedPasswordStorage.class);

                // If the stored hash is null we automatically fail validation
                if (hash != null) {
                    if (!isCredentialExpired(hash)) {
                        String rawPassword = new String(usernamePassword.getPassword().getValue());

                        boolean matches = this.passwordEncoder.verify(saltPassword(rawPassword, hash.getSalt()), hash.getEncodedHash());

                        if (matches) {
                            usernamePassword.setStatus(Status.VALID);
                            usernamePassword.setValidatedAgent(agent);
                        }
                    } else {
                        usernamePassword.setStatus(Status.EXPIRED);
                    }
                }
            } else {
                usernamePassword.setStatus(Status.AGENT_DISABLED);
            }
        }
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
     * <p>
     * Salt the give <code>rawPassword</code> with the specified <code>salt</code> value.
     * </p>
     *
     * @param rawPassword
     * @param salt
     * @return
     */
    private String saltPassword(String rawPassword, String salt) {
        return salt + rawPassword;
    }

    /**
     * <p>
     * Generates a random string to be used as a salt for passwords.
     * </p>
     *
     * @return
     */
    private String generateSalt() {
        SecureRandom pseudoRandom = null;

        try {
            pseudoRandom = SecureRandom.getInstance(DEFAULT_SALT_ALGORITHM);
            pseudoRandom.setSeed(1024);
        } catch (NoSuchAlgorithmException e) {
            throw new IdentityManagementException("Error getting SecureRandom instance: " + DEFAULT_SALT_ALGORITHM, e);
        }

        return String.valueOf(pseudoRandom.nextLong());
    }

}
