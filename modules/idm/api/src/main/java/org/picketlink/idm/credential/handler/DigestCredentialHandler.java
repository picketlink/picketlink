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

import org.picketlink.common.util.Base64;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.DigestCredentialStorage;
import org.picketlink.idm.credential.util.DigestUtil;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import java.util.Date;
import java.util.List;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.credential.util.CredentialUtils.isCurrentCredential;
import static org.picketlink.idm.credential.util.DigestUtil.calculateA2;
import static org.picketlink.idm.credential.util.DigestUtil.calculateDigest;

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
@SupportsCredentials(
        credentialClass = { DigestCredentials.class, Digest.class },
        credentialStorage = DigestCredentialStorage.class)
public class DigestCredentialHandler<S,V,U>
    extends AbstractCredentialHandler<CredentialStore<?>, DigestCredentials, Digest> {

    @Override
    protected Account getAccount(final IdentityContext context, final DigestCredentials credentials) {
        return getAccount(context, credentials.getDigest().getUsername());
    }

    @Override
    protected CredentialStorage getCredentialStorage(final IdentityContext context, final Account account, final DigestCredentials credentials, final CredentialStore<?> store) {
        List<DigestCredentialStorage> storages = store.retrieveCredentials(context, account,
                DigestCredentialStorage.class);

        for (DigestCredentialStorage storage : storages) {
            if (storage.getRealm().equals(credentials.getDigest().getRealm()) && isCurrentCredential(storage)) {
                return storage;
            }
        }

        return null;
    }

    @Override
    protected boolean validateCredential(final CredentialStorage credentialStorage, final DigestCredentials credentials) {
        DigestCredentialStorage currentCredential = (DigestCredentialStorage) credentialStorage;
        Digest digest = credentials.getDigest();

        if (currentCredential != null) {
            String providedDigest = digest.getDigest();
            String expectedDigest = null;

            if (digest.getMethod() != null && digest.getUri() != null) {
                byte[] storedHA1 = currentCredential.getHa1();
                byte[] ha2 = calculateA2(digest.getMethod(), digest.getUri());

                expectedDigest = calculateDigest(digest, storedHA1, ha2);
            } else {
                expectedDigest = String.valueOf(Base64.encodeBytes(currentCredential.getHa1()));
            }

            return expectedDigest.equals(providedDigest);
        }

        return false;
    }

    @Override
    public void update(IdentityContext context, Account account, Digest digest, CredentialStore<?> store,
            Date effectiveDate, Date expiryDate) {
        if (isNullOrEmpty(digest.getRealm())) {
            throw MESSAGES.credentialDigestInvalidRealm();
        }

        if (isNullOrEmpty(digest.getPassword())) {
            throw MESSAGES.credentialInvalidPassword();
        }

        Agent agent = (Agent) account;

        byte[] ha1 = DigestUtil.calculateA1(agent.getLoginName(), digest.getRealm(), digest.getPassword()
                .toCharArray());

        DigestCredentialStorage storage = new DigestCredentialStorage(ha1, digest.getRealm());

        if (effectiveDate != null) {
            storage.setEffectiveDate(effectiveDate);
        }

        storage.setExpiryDate(expiryDate);

        store.storeCredential(context, account, storage);
    }

}
