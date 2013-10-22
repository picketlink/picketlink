package org.picketlink.idm.credential.storage;

import org.picketlink.idm.credential.storage.annotations.Stored;

import java.util.Date;

/**
 * @author Pedro Igor
 */
public abstract class AbstractCredentialStorage implements CredentialStorage {

    private Date effectiveDate = new Date();
    private Date expiryDate;

    @Override
    @Stored
    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @Override
    @Stored
    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("effectiveDate: ").append(this.effectiveDate)
                .append(",")
                .append("expiryDate:").append(this.expiryDate).toString();
    }
}
