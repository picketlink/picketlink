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

import java.util.Date;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.password.internal.SHASaltedPasswordEncoder;
import org.picketlink.idm.password.internal.SHASaltedPasswordStorage;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * This particular implementation supports the validation of {@link UsernamePasswordCredentials}, and updating {@link Password}
 * credentials.
 * </p>
 * <p>
 * Passwords can be encoded or not. This behavior is configured by setting the <code>encodedPassword</code> property of the
 * {@link Password}.
 * </p>
 * 
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ UsernamePasswordCredentials.class, Password.class })
public class PasswordCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        CredentialStore store = validateCredentialStore(identityStore);

        if (!UsernamePasswordCredentials.class.isInstance(credentials)) {
            throw MESSAGES.unsupportedCredentialType(credentials.getClass());
        }

        UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials) credentials;

        usernamePassword.setStatus(Status.INVALID);

        Agent agent = identityStore.getAgent(usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            SHASaltedPasswordStorage hash = store.retrieveCurrentCredential(agent, SHASaltedPasswordStorage.class);

            // If the stored hash is null we automatically fail validation
            if (hash != null) {
                if (!isCredentialExpired(hash)) {
                    SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
                    String encoded = encoder.encodePassword(hash.getSalt(), new String(usernamePassword.getPassword()
                            .getValue()));

                    if (hash.getEncodedHash().equals(encoded)) {
                        usernamePassword.setStatus(Status.VALID);
                        usernamePassword.setValidatedAgent(agent);
                    }
                } else {
                    usernamePassword.setStatus(Status.EXPIRED);
                }
            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        CredentialStore store = validateCredentialStore(identityStore);

        if (!Password.class.isInstance(credential)) {
            throw MESSAGES.unsupportedCredentialType(credential.getClass());
        }

        Password password = (Password) credential;

        SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
        SHASaltedPasswordStorage hash = new SHASaltedPasswordStorage();

        hash.setEncodedHash(encoder.encodePassword(hash.getSalt(), new String(password.getValue())));
        hash.setEffectiveDate(effectiveDate);

        if (expiryDate != null) {
            hash.setExpiryDate(expiryDate);
        }

        store.storeCredential(agent, hash);
    }

    private CredentialStore validateCredentialStore(IdentityStore<?> identityStore) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw MESSAGES.invalidCredentialStoreType(identityStore.getClass());
        } else {
            return (CredentialStore) identityStore;
        }
    }
}
