package org.picketlink.idm.credential.spi;

import java.util.Date;

import org.picketlink.idm.credential.spi.annotations.Stored;

/**
 * A marker interface that indicates a Class is used to store credential related state
 * 
 * @author Shane Bryzak
 *
 */
public interface CredentialStorage {

    /**
     * Return the Date from when the credential becomes effective.  A result of null means the credential has
     * no effective date (and is current as long as the expiry date is either null, or in the future).
     *  
     * @return
     */
    @Stored Date getEffectiveDate();

    /**
     * Return the Date when the credential expires.  A result of null means the credential has no expiry date.
     * 
     * @return
     */
    @Stored Date getExpiryDate();
}
