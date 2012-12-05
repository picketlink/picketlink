package org.picketlink.idm.credential.spi;

import org.picketlink.idm.credential.LoginCredentials;
import org.picketlink.idm.spi.IdentityStore;

/**
 * This factory is responsible for returning CredentialHandler instances for
 * a given LoginCredentials class and IdentityStore class
 *
 * @author Shane Bryzak
 */
public interface CredentialHandlerFactory {

    CredentialHandler getCredentialHandler(Class<? extends LoginCredentials> credentialsClass, 
            Class<? extends IdentityStore> identityStoreClass);
}
