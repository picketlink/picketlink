package org.picketlink.idm.credential.spi;

import java.util.Date;

/**
 * A marker interface that indicates a Class is used to store credential related state
 * 
 * @author Shane Bryzak
 *
 */
public interface CredentialStorage {
    Date getExpiryDate();
}
