package org.picketlink.idm.credential.spi;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.User;
import org.picketlink.idm.spi.IdentityStore;

/**
 * Performs credential validation and persists credential state to an IdentityStore.
 *
 * @author Shane Bryzak
 */
public interface CredentialHandler {
    /**
     * 
     * @param credentials
     * @param store
     * @return
     */
    User validate(Credentials credentials, IdentityStore store);

    /**
     * 
     * @param user
     * @param credential
     * @param store
     */
    void update(Agent agent, Object credential, IdentityStore store);
}
