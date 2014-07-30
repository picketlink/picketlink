package org.picketlink.idm.spi;

/**
 * <p>Context initializers can be used populate an specific {@link org.picketlink.idm.spi.IdentityContext} instance
 * with additional state.</p>
 *
 * <p>Usually, this is useful when a specific {@link org.picketlink.idm.spi.IdentityStore} requires a specific resource or state
 * in order to perform an identity operation.</p>
 *
 * <p>They can be provided at configuration time using the {@link org.picketlink.idm.config.IdentityStoreConfigurationBuilder#addContextInitializer(ContextInitializer)}.</p>
 *
 * @author Shane Bryzak
 *
 */
public interface ContextInitializer {

    /**
     * <p>This method is invoked once and right after the {@link org.picketlink.idm.spi.IdentityContext} is created.</p>
     *
     * @param context
     * @param store
     */
    void initContextForStore(IdentityContext context, IdentityStore<?> store);
}
