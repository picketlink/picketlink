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
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.X509CertificateCredentials;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.X509CertificateStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * This particular implementation supports the validation of {@link X509CertificateCredentials}, and updating {@link
 * X509Cert} credentials.
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 */
@SupportsCredentials(
        credentialClass = {X509CertificateCredentials.class, X509Certificate.class},
        credentialStorage = X509CertificateStorage.class)
public class X509CertificateCredentialHandler<S, V, U>
        extends AbstractCredentialHandler<CredentialStore<?>, X509CertificateCredentials, X509Certificate> {

    @Override
    protected X509CertificateStorage getCredentialStorage(final IdentityContext context, Account account, final X509CertificateCredentials
            credentials, final CredentialStore<?> store) {
        return store.retrieveCurrentCredential(context, account, X509CertificateStorage.class);
    }

    @Override
    protected boolean validateCredential(final CredentialStorage storage, final X509CertificateCredentials credentials) {
        X509CertificateStorage certificateStorage = (X509CertificateStorage) storage;

        if (!credentials.isTrusted()) {
            try {
                byte[] certBytes = Base64.decode(certificateStorage.getBase64Cert());
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                X509Certificate storedCert = (X509Certificate) certFactory
                        .generateCertificate(new ByteArrayInputStream(certBytes));
                X509Certificate providedCert = credentials.getCertificate();

                return storedCert.equals(providedCert);
            } catch (Exception e) {
                throw new IdentityManagementException("Error while checking user's certificate.", e);
            }
        }

        return true;
    }

    @Override
    protected Account getAccount(final IdentityContext context, final X509CertificateCredentials credentials) {
        return getAccount(context, credentials.getUsername());
    }

    @Override
    public void update(IdentityContext context, Account account, X509Certificate cert, CredentialStore<?> store,
                       Date effectiveDate, Date expiryDate) {
        X509CertificateStorage storage = new X509CertificateStorage(cert);

        if (effectiveDate != null) {
            storage.setEffectiveDate(effectiveDate);
        }

        storage.setExpiryDate(expiryDate);

        store.storeCredential(context, account, storage);
    }

}