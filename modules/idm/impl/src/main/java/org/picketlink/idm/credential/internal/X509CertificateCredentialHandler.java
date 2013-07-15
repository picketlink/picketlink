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

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import org.picketlink.common.util.Base64;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.X509CertificateCredentials;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

/**
 * This particular implementation supports the validation of {@link X509CertificateCredentials}, and updating {@link X509Cert}
 * credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ X509CertificateCredentials.class, X509Certificate.class })
public class X509CertificateCredentialHandler<S,V,U>
    extends AbstractCredentialHandler<CredentialStore<?>,X509CertificateCredentials, X509Certificate> {

    @Override
    public void setup(CredentialStore<?> identityStore) {
    }

    @Override
    public void validate(IdentityContext context, X509CertificateCredentials credentials,
            CredentialStore<?> store) {
        Agent agent = getAgent(context, credentials.getUsername());

        credentials.setStatus(Status.INVALID);
        credentials.setValidatedAgent(null);

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            if (agent.isEnabled()) {
                boolean isValid = credentials.isTrusted();

                if (!credentials.isTrusted()) {
                    X509CertificateStorage storage = store.retrieveCurrentCredential(context, agent, X509CertificateStorage.class);

                    if (storage != null) {
                        String base64Cert = storage.getBase64Cert();

                        byte[] certBytes = Base64.decode(base64Cert);

                        try {
                            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                            X509Certificate storedCert = (X509Certificate) certFactory
                                    .generateCertificate(new ByteArrayInputStream(certBytes));
                            X509Certificate providedCert = credentials.getCertificate();

                            isValid = storedCert.equals(providedCert);
                        } catch (Exception e) {
                            throw new IdentityManagementException("Error while checking user's certificate.", e);
                        }
                    }
                }

                if (isValid) {
                    credentials.setStatus(Status.VALID);
                    credentials.setValidatedAgent(agent);
                }

            } else {
                credentials.setStatus(Status.AGENT_DISABLED);
            }
        }
    }

    @Override
    public void update(IdentityContext context, Account account, X509Certificate cert, CredentialStore<?> store,
            Date effectiveDate, Date expiryDate) {
        X509CertificateStorage storage = new X509CertificateStorage(cert);
        store.storeCredential(context, account, storage);
    }
}