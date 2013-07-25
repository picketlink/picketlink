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

import javax.persistence.Entity;
import org.picketlink.idm.credential.storage.DigestCredentialStorage;
import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;

/**
 * <p>This entity is mapped to support DIGEST credential types using a {@link org.picketlink.idm.credential.storage.DigestCredentialStorage}.</p>
 *
 * @author pedroigor
 */
@ManagedCredential (DigestCredentialStorage.class)
@Entity
public class DigestCredentialTypeEntity extends AbstractCredentialTypeEntity {

    private static final long serialVersionUID = 8582138093637065019L;

    @CredentialProperty (name = "realm")
    private String digestRealm;

    @CredentialProperty (name = "ha1")
    private byte[] digestHa1;

    public String getDigestRealm() {
        return digestRealm;
    }

    public void setDigestRealm(String digestRealm) {
        this.digestRealm = digestRealm;
    }

    public byte[] getDigestHa1() {
        return digestHa1;
    }

    public void setDigestHa1(byte[] digestHa1) {
        this.digestHa1 = digestHa1;
    }
}
