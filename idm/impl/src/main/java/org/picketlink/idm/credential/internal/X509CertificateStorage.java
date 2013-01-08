/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.credential.internal;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.X509Cert;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;
import org.picketlink.idm.internal.util.Base64;

/**
 * <p> {@link CredentialStorage} for {@link X509Certificate} credentials.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class X509CertificateStorage implements CredentialStorage {

    private Date effectiveDate;
    private Date expiryDate;
    private String base64Cert;

    public X509CertificateStorage() { }

    public X509CertificateStorage(X509Cert credential) {
        try {
            this.base64Cert = Base64.encodeBytes(credential.getValue().getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IdentityManagementException("Could not get Base64 representation for X509 Certificate.", e);
        }
    }

    @Override @Stored
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @Override @Stored
    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Stored
    public String getBase64Cert() {
        return this.base64Cert;
    }

    public void setBase64Cert(String base64Cert) {
        this.base64Cert = base64Cert;
    }

}