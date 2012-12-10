package org.picketlink.idm.credential.spi;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.spi.IdentityStore;

/**
 * This factory is responsible for returning CredentialHandler instances for
 * a given LoginCredentials class and IdentityStore class
 *
 * @author Shane Bryzak
 */
public interface CredentialHandlerFactory {

    CredentialHandler getCredentialValidator(Class<? extends Credentials> credentialsClass, 
            Class<? extends IdentityStore> identityStoreClass);

    void setCredentialValidator(Class<? extends Credentials> credentialsClass, 
            Class<? extends IdentityStore> identityStoreClass, CredentialHandler handler);

    CredentialHandler getCredentialUpdater(Class<?> credentialClass, 
            Class<? extends IdentityStore> identityStoreClass);

    void setCredentialHandler(Class<?> credentialClass, Class<? extends IdentityStore> identityStoreClass, 
            CredentialHandler handler);
}
