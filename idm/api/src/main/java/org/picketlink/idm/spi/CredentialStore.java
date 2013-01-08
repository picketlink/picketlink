package org.picketlink.idm.spi;

import org.picketlink.idm.credential.spi.CredentialStorage;

/**
 * An optional interface typically implemented by an IdentityStore that supports the storage of credential related state 
 * 
 * @author Shane Bryzak
 *
 */
public interface CredentialStore {
    /**
     * Store the specified credential state
     * 
     * @param storage
     */
    void storeCredential(CredentialStorage storage);

    /**
     * Return the currently active credential state of the specified class
     * 
     * @param storageClass
     * @return
     */
    CredentialStorage retrieveCurrentCredential(Class<? extends CredentialStorage> storageClass);
}
