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

package org.picketlink.idm.credential.storage;

import org.picketlink.common.util.Base64;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.storage.annotations.Stored;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * <p> {@link CredentialStorage} for X509 Certificates credentials.</p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class X509CertificateStorage extends AbstractCredentialStorage {

    private String base64Cert;

    public X509CertificateStorage() { }

    public X509CertificateStorage(X509Certificate certificate) {
        try {
            this.base64Cert = Base64.encodeBytes(certificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IdentityManagementException("Could not get Base64 representation for X509 Certificate.", e);
        }
    }

    @Stored
    public String getBase64Cert() {
        return this.base64Cert;
    }

    public void setBase64Cert(String base64Cert) {
        this.base64Cert = base64Cert;
    }

}