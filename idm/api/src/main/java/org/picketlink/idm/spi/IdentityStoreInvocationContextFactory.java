package org.picketlink.idm.spi;

/**
 * Factory for providing IdentityStoreInvocationContext instances
 *  
 * @author Shane Bryzak
 */
public interface IdentityStoreInvocationContextFactory {
    IdentityStoreInvocationContext getContext(IdentityStore store);
}
