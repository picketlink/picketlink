package org.picketlink.idm.spi;

/**
 * Factory for creating and initializing IdentityStoreInvocationContext instances
 *  
 * @author Shane Bryzak
 */
public interface IdentityStoreInvocationContextFactory {
    IdentityStoreInvocationContext createContext();

    void initContextForStore(IdentityStoreInvocationContext ctx, IdentityStore store);
}
