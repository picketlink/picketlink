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
    @Stored Date getExpiryDate();
}
