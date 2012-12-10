package org.picketlink.idm.spi;

import org.picketlink.idm.credential.spi.CredentialHandlerFactory;

/**
 * Factory for creating and initializing IdentityStoreInvocationContext instances
 *  
 * @author Shane Bryzak
 */
public interface IdentityStoreInvocationContextFactory {
    IdentityStoreInvocationContext createContext();

    void setCredentialHandlerFactory(CredentialHandlerFactory factory);

    CredentialHandlerFactory getCredentialHandlerFactory();

    void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore store);
}
