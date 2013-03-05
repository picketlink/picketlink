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

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.credential.internal.CredentialUtils.isCurrentCredential;
import static org.picketlink.idm.credential.internal.CredentialUtils.isLastCredentialExpired;
import static org.picketlink.idm.credential.internal.DigestUtil.calculateA2;
import static org.picketlink.idm.credential.internal.DigestUtil.calculateDigest;

import java.util.Date;
import java.util.List;

import org.picketlink.common.util.Base64;
import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * This particular implementation supports the validation of {@link DigestCredentials}.
 * </p>
 * <p>
 * When using this handler, password are always stored using: H(A1) = MD5 (unq(username) ":" unq(realm) ":" password). During
 * the validation this handler will use the stored HA1 to compare with the digest provided by the {@link Digest} credential.
 * This is done in two ways, if the credential has the method and uri setted the H(A2) will also be calculated and used to
 * calcutate the final digest as KD ( H(A1), unq(nonce-value) ":" nc-value ":" unq(cnonce-value) ":" unq(qop-value) ":" H(A2) ).
 * </p>
 * 
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ DigestCredentials.class, Digest.class })
public class DigestCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        CredentialStore credentialStore = validateCredentialStore(identityStore);

        if (!DigestCredentials.class.isInstance(credentials)) {
            throw MESSAGES.credentialUnsupportedType(credentials.getClass(), this);
        }

        DigestCredentials digestCredential = (DigestCredentials) credentials;

        digestCredential.setStatus(Status.INVALID);

        Digest digest = digestCredential.getDigest();
        Agent agent = identityStore.getAgent(digest.getUsername());

        if (agent != null) {
            List<DigestCredentialStorage> storages = credentialStore.retrieveCredentials(agent, DigestCredentialStorage.class);
            DigestCredentialStorage currentCredential = null;

            for (DigestCredentialStorage storage : storages) {
                if (storage.getRealm().equals(digest.getRealm()) && isCurrentCredential(storage)) {
                    currentCredential = storage;
                    break;
                }
            }

            if (currentCredential != null) {
                if (digest.getMethod() != null && digest.getUri() != null) {
                    byte[] storedHA1 = currentCredential.getHa1();
                    byte[] ha2 = calculateA2(digest.getMethod(), digest.getUri());

                    String calculateDigest = calculateDigest(digest, storedHA1, ha2);

                    if (calculateDigest.equals(digest.getDigest())) {
                        digestCredential.setStatus(Status.VALID);
                    }
                } else {
                    String storedDigestPassword = Base64.encodeBytes(currentCredential.getHa1());
                    String providedDigest = digest.getDigest();

                    if (String.valueOf(storedDigestPassword).equals(providedDigest)) {
                        digestCredential.setStatus(Status.VALID);
                    }
                }
            } else if (isLastCredentialExpired(agent, credentialStore, DigestCredentialStorage.class)) {
                digestCredential.setStatus(Status.EXPIRED);
            }
            
            if (digestCredential.getStatus().equals(Status.VALID)) {
                digestCredential.setValidatedAgent(agent);
            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        CredentialStore credentialStore = validateCredentialStore(identityStore);

        if (!Digest.class.isInstance(credential)) {
            throw MESSAGES.credentialUnsupportedType(credential.getClass(), this);
        }

        Digest digestCredential = (Digest) credential;

        if (isNullOrEmpty(digestCredential.getRealm())) {
            throw MESSAGES.credentialDigestInvalidRealm();
        }

        if (StringUtil.isNullOrEmpty(digestCredential.getPassword())) {
            throw MESSAGES.credentialInvalidPassword();
        }

        byte[] ha1 = DigestUtil.calculateA1(agent.getLoginName(), digestCredential.getRealm(), digestCredential.getPassword()
                .toCharArray());

        DigestCredentialStorage storage = new DigestCredentialStorage(ha1, digestCredential.getRealm());

        storage.setEffectiveDate(effectiveDate);
        storage.setExpiryDate(expiryDate);

        credentialStore.storeCredential(agent, storage);
    }

    private CredentialStore validateCredentialStore(IdentityStore<?> identityStore) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw MESSAGES.credentialInvalidCredentialStoreType(identityStore.getClass());
        } else {
            return (CredentialStore) identityStore;
        }
    }
}
