package org.picketlink.idm.spi;


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
}
