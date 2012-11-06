package org.picketlink.idm.spi;

import org.picketlink.idm.config.IdentityStoreConfiguration;

/**
 * Creates IdentityStore instances based on a provided configuration
 * 
 * @author Shane Bryzak
 *
 */
public interface IdentityStoreFactory {
    /**
     * Creates an instance of an IdentityStore using the provided configuration
     * 
     * @param config
     * @return
     */
    IdentityStore createIdentityStore(IdentityStoreConfiguration config);

    /**
     * Maps specific implementations of IdentityStoreConfiguration to a corresponding
     * IdentityStore implementation.
     * 
     * @param configClass
     * @param storeClass
     */
    void mapConfiguration(Class<? extends IdentityStoreConfiguration> configClass, 
            Class<? extends IdentityStore> storeClass);
}
