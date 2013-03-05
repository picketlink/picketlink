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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.picketlink.common.util.Base64;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.X509Cert;
import org.picketlink.idm.credential.X509CertificateCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;

/**
 * This particular implementation supports the validation of {@link X509CertificateCredentials}, and updating {@link X509Cert} credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({X509CertificateCredentials.class, X509Cert.class})
public class X509CertificateCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        validateCredentialStore(identityStore);

        if (!X509CertificateCredentials.class.isInstance(credentials)) {
            throw MESSAGES.credentialUnsupportedType(credentials.getClass(), this);
        }

        X509CertificateCredentials certCredentials = (X509CertificateCredentials) credentials;

        Agent agent = identityStore.getAgent(certCredentials.getUsername());
        
        certCredentials.setStatus(Status.INVALID);
        
        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            CredentialStore store = (CredentialStore) identityStore;
            
            X509CertificateStorage storage = store.retrieveCurrentCredential(agent, X509CertificateStorage.class);

            if (storage != null) {
                String base64Cert = storage.getBase64Cert();
                
                byte[] certBytes = Base64.decode(base64Cert);

                try {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    X509Certificate storedCert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(
                            certBytes));
                    X509Cert providedCert = certCredentials.getCertificate();

                    if (storedCert.equals(providedCert.getValue())) {
                        certCredentials.setStatus(Status.VALID); 
                    }
                } catch (Exception e) {
                    throw new IdentityManagementException("Error while checking user's certificate.", e);
                }
            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        validateCredentialStore(identityStore);

        if (!X509Cert.class.isInstance(credential)) {
            throw MESSAGES.credentialUnsupportedType(credential.getClass(), this);
        }

        X509Cert certificate = (X509Cert) credential;
        X509CertificateStorage storage = new X509CertificateStorage((X509Cert) certificate);
        
        CredentialStore store = (CredentialStore) identityStore;
        
        store.storeCredential(agent, storage);
    }

    private void validateCredentialStore(IdentityStore<?> identityStore) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw MESSAGES.credentialInvalidCredentialStoreType(identityStore.getClass());
        }
    }

}
