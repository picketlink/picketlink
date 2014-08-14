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

import org.picketlink.idm.IdentityManagementException;

import javax.naming.ldap.LdapName;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A credential representing an X509 certificate for certificate-based authentication
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
public class X509CertificateCredentials extends AbstractBaseCredentials implements Credentials {

    private String subjectRegex;
    private X509Certificate certificate;
    private String userName;
    private boolean trusted;

    public X509CertificateCredentials(X509Certificate certificate) {
        this(certificate, null);
    }

    public X509CertificateCredentials(X509Certificate certificate, String subjectRegex) {
        this.certificate = certificate;
        this.subjectRegex = subjectRegex;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public String getUsername() {
        if (this.userName == null) {
            this.userName = getCertificatePrincipal().getName();

            if (subjectRegex == null) {

                try {
                    LdapName ldapName = new LdapName(this.userName);
                    this.userName = ldapName.getRdn(ldapName.size() - 1).getValue().toString();
                } catch (Exception e) {
                    throw new IdentityManagementException("Could not extract CN from X509.", e);
                }
            } else {
                Matcher matcher = Pattern.compile(this.subjectRegex).matcher(this.userName);

                if (matcher.find())
                    if (matcher.groupCount() != 1) {
                        throw new IdentityManagementException("Single group expected from expression.");
                    }

                    this.userName = matcher.group(1);
            }
        }

        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void invalidate() {
        this.certificate = null;
        this.subjectRegex = null;
        this.trusted = false;
        this.userName = null;
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
