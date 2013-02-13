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

package org.picketlink.idm.password.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;

/**
 * Represents the encoded hash value stored by an IdentityStore
 *  
 * @author Shane Bryzak
 */
public class SHASaltedPasswordStorage implements CredentialStorage {

    private Date effectiveDate;
    private Date expiryDate;
    private String encodedHash;
    private String salt;

    public SHASaltedPasswordStorage() {
        this.salt = generateSalt();
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
    public String getEncodedHash() {
        return encodedHash;
    }

    public void setEncodedHash(String encodedHash) {
        this.encodedHash = encodedHash;
    }
    
    @Stored
    public String getSalt() {
        return this.salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
    }

    private String generateSalt() {
        String salt = null;

        SecureRandom pseudoRandom = null;
        String algorithm = "SHA1PRNG";

        try {
            pseudoRandom = SecureRandom.getInstance(algorithm);
            pseudoRandom.setSeed(1024);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error getting SecureRandom instance: " + algorithm, e);
        }

        salt = String.valueOf(pseudoRandom.nextLong());

        return salt;
    }
}
