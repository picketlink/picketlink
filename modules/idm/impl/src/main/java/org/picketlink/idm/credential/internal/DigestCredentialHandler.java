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
import java.util.List;
import org.picketlink.common.util.Base64;
import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.SecurityContext;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.credential.internal.CredentialUtils.isCurrentCredential;
import static org.picketlink.idm.credential.internal.CredentialUtils.isLastCredentialExpired;
import static org.picketlink.idm.credential.internal.DigestUtil.calculateA2;
import static org.picketlink.idm.credential.internal.DigestUtil.calculateDigest;

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
public class DigestCredentialHandler<S,V,U>
    implements CredentialHandler<CredentialStore<?>, DigestCredentials, Digest> {

    @Override
    public void setup(CredentialStore<?> identityStore) {
    }

    @Override
    public void validate(SecurityContext context, DigestCredentials credentials, CredentialStore<?> store) {

        if (!DigestCredentials.class.isInstance(credentials)) {
            throw MESSAGES.credentialUnsupportedType(credentials.getClass(), this);
        }

        DigestCredentials digestCredential = (DigestCredentials) credentials;

        digestCredential.setStatus(Status.INVALID);
        digestCredential.setValidatedAgent(null);

        Digest digest = digestCredential.getDigest();
        Agent agent = store.getAgent(context, digest.getUsername());

        if (agent != null) {
            if (agent.isEnabled()) {
                List<DigestCredentialStorage> storages = store.retrieveCredentials(context, agent,
                        DigestCredentialStorage.class);
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
                } else if (isLastCredentialExpired(context, agent, store, DigestCredentialStorage.class)) {
                    digestCredential.setStatus(Status.EXPIRED);
                }
            } else {
                digestCredential.setStatus(Status.AGENT_DISABLED);
            }

            if (digestCredential.getStatus().equals(Status.VALID)) {
                digestCredential.setValidatedAgent(agent);
            }
        }
    }

    @Override
    public void update(SecurityContext context, Agent agent, Digest digest, CredentialStore<?> store,
            Date effectiveDate, Date expiryDate) {
        if (isNullOrEmpty(digest.getRealm())) {
            throw MESSAGES.credentialDigestInvalidRealm();
        }

        if (StringUtil.isNullOrEmpty(digest.getPassword())) {
            throw MESSAGES.credentialInvalidPassword();
        }

        byte[] ha1 = DigestUtil.calculateA1(agent.getLoginName(), digest.getRealm(), digest.getPassword()
                .toCharArray());

        DigestCredentialStorage storage = new DigestCredentialStorage(ha1, digest.getRealm());

        storage.setEffectiveDate(effectiveDate);
        storage.setExpiryDate(expiryDate);

        store.storeCredential(context, agent, storage);
    }

}
