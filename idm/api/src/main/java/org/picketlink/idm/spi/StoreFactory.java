package org.picketlink.idm.spi;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.PartitionStoreConfiguration;

/**
 * Creates IdentityStore instances based on a provided configuration
 * 
 * @author Shane Bryzak
 *
 */
@SuppressWarnings("rawtypes")
public interface StoreFactory {
    /**
     * Creates an instance of an IdentityStore using the provided configuration
     * 
     * @param config
     * @return
     */
    IdentityStore createIdentityStore(IdentityStoreConfiguration config, IdentityStoreInvocationContext context);

    /**
     * Creates an instance of a PartitionStore using the provided configuration
     * 
     * @param config
     * @return
     */
    PartitionStore createPartitionStore(PartitionStoreConfiguration config);

    /**
     * Maps specific implementations of IdentityStoreConfiguration to a corresponding
     * IdentityStore implementation.
     * 
     * @param configClass
     * @param storeClass
     */
    void mapIdentityConfiguration(Class<? extends IdentityStoreConfiguration> configClass, 
            Class<? extends IdentityStore> storeClass);

    /**
     * Maps specific implementations of PartitionStoreConfiguration to a corresponding
     * PartitionStore implementation
     * 
     * @param configClass
     * @param storeClass
     */
    void mapPartitionConfiguration(Class<? extends PartitionStoreConfiguration> configClass,
            Class<? extends PartitionStore> storeClass);
}
