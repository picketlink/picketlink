package org.picketlink.idm.spi;

import org.picketlink.idm.credential.spi.CredentialHandlerFactory;

/**
 * Factory for creating and initializing IdentityStoreInvocationContext instances
 *  
 * @author Shane Bryzak
 */
public interface IdentityStoreInvocationContextFactory {
    /**
     * 
     * @return
     */
    IdentityStoreInvocationContext createContext();

    /**
     * 
     * @param ctx
     * @param store
     */
    void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore<?> store);

    void initContextForStore(IdentityStoreInvocationContext context, PartitionStore<?> store);
}
