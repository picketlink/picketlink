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
import org.picketlink.idm.credential.storage.OTPCredentialStorage;
import org.picketlink.idm.jpa.annotations.CredentialProperty;
import org.picketlink.idm.jpa.annotations.entity.ManagedCredential;

/**
 * <p>This entity is mapped to support OTP credential types using a {@link org.picketlink.idm.credential.storage.OTPCredentialStorage}.</p>
 *
 * @author pedroigor
 */
@ManagedCredential (OTPCredentialStorage.class)
@Entity
public class OTPCredentialTypeEntity extends AbstractCredentialTypeEntity {

    private static final long serialVersionUID = 2178549213245407363L;

    @CredentialProperty (name = "secretKey")
    private String totpSecretKey;

    @CredentialProperty (name = "device")
    private String totpDevice;

    public String getTotpSecretKey() {
        return totpSecretKey;
    }

    public void setTotpSecretKey(String totpSecretKey) {
        this.totpSecretKey = totpSecretKey;
    }

    public String getTotpDevice() {
        return totpDevice;
    }

    public void setTotpDevice(String totpDevice) {
        this.totpDevice = totpDevice;
    }
}
