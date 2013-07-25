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
package org.picketlink.idm.jpa.model.sample.simple;

import javax.persistence.Column;
import javax.persistence.Entity;
import org.picketlink.idm.credential.storage.X509CertificateStorage;
import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;

/**
 * <p>This entity is mapped to support X509 credential types using a {@link X509CertificateStorage}.</p>
 *
 * @author pedroigor
 */
@ManagedCredential (X509CertificateStorage.class)
@Entity
public class X509CredentialTypeEntity extends AbstractCredentialTypeEntity {

    private static final long serialVersionUID = -8313462190592256324L;

    @CredentialProperty
    @Column(length = 1024)
    private String base64Cert;

    public String getBase64Cert() {
        return base64Cert;
    }

    public void setBase64Cert(String base64Cert) {
        this.base64Cert = base64Cert;
    }
}
