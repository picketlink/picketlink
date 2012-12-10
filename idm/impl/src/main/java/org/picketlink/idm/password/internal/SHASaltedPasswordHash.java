package org.picketlink.idm.password.internal;

import java.util.Date;

import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.Stored;

/**
 * Represents the encoded hash value stored by an IdentityStore
 *  
 * @author Shane Bryzak
 */
public class SHASaltedPasswordHash implements CredentialStorage {

    private Date expiryDate;
    private String encodedHash;

    @Override
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
}
