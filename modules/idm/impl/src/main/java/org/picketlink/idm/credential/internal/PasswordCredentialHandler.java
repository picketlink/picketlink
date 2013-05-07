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

import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.credential.internal.CredentialUtils.isCredentialExpired;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.password.PasswordEncoder;
import org.picketlink.idm.password.internal.EncodedPasswordStorage;
import org.picketlink.idm.password.internal.SHAPasswordEncoder;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;

/**
 * <p>
 * This particular implementation supports the validation of {@link UsernamePasswordCredentials}, and updating {@link Password}
 * credentials.
 * </p>
 * <p>
 *
 * <p>
 * How passwords are encoded can be changed by specifying a configuration option using the <code>PASSWORD_ENCODER</code>. By
 * default a SHA-512 encoding is performed.
 * </p>
 *
 * <p>
 * Password are always salted before encoding.
 * </p>
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ UsernamePasswordCredentials.class, Password.class })
public class PasswordCredentialHandler implements CredentialHandler {

    private static final String DEFAULT_SALT_ALGORITHM = "SHA1PRNG";

    /**
     * <p>
     * Stores a <b>stateless</b> instance of {@link PasswordEncoder} that should be used to encode passwords.
     * </p>
     */
    public static final String PASSWORD_ENCODER = "PASSWORD_ENCODER";

    private PasswordEncoder passwordEncoder = new SHAPasswordEncoder(512);

    @Override
    public void setup(IdentityStore<?> identityStore) {
        Map<String, Object> options = identityStore.getConfig().getCredentialHandlerProperties();

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
    public void validate(SecurityContext context, Credentials credentials, IdentityStore<?> identityStore) {
        CredentialStore store = validateCredentialStore(identityStore);

        if (!UsernamePasswordCredentials.class.isInstance(credentials)) {
            throw MESSAGES.credentialUnsupportedType(credentials.getClass(), this);
        }

        UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials) credentials;

        usernamePassword.setStatus(Status.INVALID);

        Agent agent = identityStore.getAgent(context, usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            if (agent.isEnabled()) {
                EncodedPasswordStorage hash = store.retrieveCurrentCredential(context, agent, EncodedPasswordStorage.class);

                // If the stored hash is null we automatically fail validation
                if (hash != null) {
                    if (!isCredentialExpired(hash)) {
                        String rawPassword = new String(usernamePassword.getPassword().getValue());

                        String encoded = this.passwordEncoder.encode(saltPassword(rawPassword, hash.getSalt()));

                        if (hash.getEncodedHash().equals(encoded)) {
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
    public void update(SecurityContext context, Agent agent, Object credential, IdentityStore<?> identityStore,
            Date effectiveDate, Date expiryDate) {
        CredentialStore store = validateCredentialStore(identityStore);

        if (!Password.class.isInstance(credential)) {
            throw MESSAGES.credentialUnsupportedType(credential.getClass(), this);
        }

        Password password = (Password) credential;

        EncodedPasswordStorage hash = new EncodedPasswordStorage();

        String rawPassword = new String(password.getValue());

        String passwordSalt = generateSalt();

        hash.setSalt(passwordSalt);
        hash.setEncodedHash(this.passwordEncoder.encode(saltPassword(rawPassword, passwordSalt)));
        hash.setEffectiveDate(effectiveDate);

        if (expiryDate != null) {
            hash.setExpiryDate(expiryDate);
        }

        store.storeCredential(context, agent, hash);
    }

    private CredentialStore validateCredentialStore(IdentityStore<?> identityStore) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw MESSAGES.credentialInvalidCredentialStoreType(identityStore.getClass());
        } else {
            return (CredentialStore) identityStore;
        }
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
