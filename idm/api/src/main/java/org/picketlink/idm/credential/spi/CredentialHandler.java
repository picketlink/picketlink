package org.picketlink.idm.credential.spi;

import java.util.Date;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.model.Agent;
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
    void validate(Credentials credentials, IdentityStore<?> store);

    /**
     * 
     * @param user
     * @param credential
     * @param store
     */
    void update(Agent agent, Object credential, IdentityStore<?> store, Date effectiveDate, Date expiryDate);
}
