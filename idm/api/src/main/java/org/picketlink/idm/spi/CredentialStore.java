package org.picketlink.idm.spi;

import java.util.List;

import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.Agent;

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
    void storeCredential(Agent agent, CredentialStorage storage);

    /**
     * Return the currently active credential state of the specified class, for the specified Agent
     * 
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> T retrieveCurrentCredential(Agent agent, Class<T> storageClass);

    /**
     * Returns a List of all credential state of the specified class, for the specified Agent
     * 
     * @param agent
     * @param storageClass
     * @return
     */
    <T extends CredentialStorage> List<T> retrieveCredentials(Agent agent, Class<T> storageClass);
}