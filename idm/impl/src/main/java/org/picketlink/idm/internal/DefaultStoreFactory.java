package org.picketlink.idm.internal;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.file.internal.FileIdentityStoreConfiguration;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;
import org.picketlink.idm.jpa.internal.JPAPartitionStore;
import org.picketlink.idm.jpa.internal.JPAPartitionStoreConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default StoreFactory implementation.  This factory is pre-configured to be
 * able to create instances of the following built-in IdentityStore implementations based 
 * on the corresponding IdentityStoreConfiguration:
 * 
 * JPAIdentityStore - JPAIdentityStoreConfiguration
 * LDAPIdentityStore - LDAPConfiguration
 * 
 * It also maps the following PartitionStore implementations:
 * 
 * JPAPartitionStore - JPAPartitionStoreConfiguration
 * 
 * @author Shane Bryzak
 */
public class DefaultStoreFactory implements StoreFactory {
    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> identityConfigMap =
            new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>>();

    private Map<Class<? extends PartitionStoreConfiguration>, Class<? extends PartitionStore>> partitionConfigMap =
            new HashMap<Class<? extends PartitionStoreConfiguration>, Class<? extends PartitionStore>>();    

    public DefaultStoreFactory() {
        identityConfigMap.put(JPAIdentityStoreConfiguration.class, JPAIdentityStore.class);
        identityConfigMap.put(LDAPConfiguration.class, LDAPIdentityStore.class);
        identityConfigMap.put(FileIdentityStoreConfiguration.class, FileBasedIdentityStore.class);
        partitionConfigMap.put(JPAPartitionStoreConfiguration.class, JPAPartitionStore.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IdentityStore<?> createIdentityStore(IdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        for (Class<? extends IdentityStoreConfiguration> cc : identityConfigMap.keySet()) {
            if (cc.isInstance(config)) {
                try {
                    IdentityStore<IdentityStoreConfiguration> store = 
                            (IdentityStore<IdentityStoreConfiguration>) identityConfigMap.get(cc).newInstance(); 
                    store.setup(config, context);
                    return store;
                } catch (InstantiationException e) {
                    throw new SecurityConfigurationException(
                            "Exception while creating new IdentityStore instance", e);
                } catch (IllegalAccessException e) {
                    throw new SecurityConfigurationException(
                            "Exception while creating new IdentityStore instance", e);                }
            }
        }

        throw new IllegalArgumentException(
                "The IdentityStoreConfiguration specified is not supported by this IdentityStoreFactory implementation");
    }

    @Override
    public PartitionStore createPartitionStore(PartitionStoreConfiguration config) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void mapIdentityConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
            Class<? extends IdentityStore> storeClass) {
        identityConfigMap.put(configClass,  (Class<? extends IdentityStore<?>>) storeClass);
    }

    @Override
    public void mapPartitionConfiguration(Class<? extends PartitionStoreConfiguration> configClass,
            Class<? extends PartitionStore> storeClass) {
        partitionConfigMap.put(configClass,  storeClass);
    }
}
