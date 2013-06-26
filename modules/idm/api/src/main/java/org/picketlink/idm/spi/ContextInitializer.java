package org.picketlink.idm.spi;

/**
 * Used to initialize store-specific context parameters
 *
 * @author Shane Bryzak
 *
 */
public interface ContextInitializer {
    /**
     *
     * @param ctx
     * @param store
     */
    void initContextForStore(IdentityContext context, IdentityStore<?> store);
}
