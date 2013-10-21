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

package org.picketlink.idm.credential;

import java.io.IOException;
import java.io.StringReader;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Properties;


/**
 * A credential representing an X509 certificate for certificate-based authentication
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class X509CertificateCredentials extends AbstractBaseCredentials implements Credentials {

    private X509Certificate certificate;
    private String userName;
    private boolean trusted;

    public X509CertificateCredentials(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public String getUsername() {
        if (this.userName == null) {
            Properties prop = new Properties();

            this.userName = getCertificatePrincipal().getName();

            try {
                prop.load(new StringReader(userName.replaceAll(",", "\n")));
            } catch (IOException e) {
                e.printStackTrace();
            }

            userName = prop.getProperty("CN");
        }

        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void invalidate() {
        certificate = null;
    }

    private Principal getCertificatePrincipal() {
        Principal certprincipal = this.certificate.getSubjectDN();

        if (certprincipal == null) {
            certprincipal = this.certificate.getIssuerDN();
        }

        return certprincipal;
    }

    /**
     * <p>Indicates if the provided certificate should be trusted and perform the validation against the existence of
     * the principal.</p>
     *
     * @param trusted
     */
    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }

    public boolean isTrusted() {
        return this.trusted;
    }

    @Override
    public String toString() {
        return "userName: " + getUsername();
    }
}
