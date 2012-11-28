package org.picketlink.idm.internal;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreFactory;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * Default IdentityStoreFactory implementation.  This factory is pre-configured to be
 * able to create instances of the following built-in IdentityStore implementations based 
 * on the corresponding IdentityStoreConfiguration:
 * 
 * JPAIdentityStore - JPAIdentityStoreConfig
 * LDAPIdentityStore - LDAPConfiguration
 * 
 * @author Shane Bryzak
 */
public class DefaultIdentityStoreFactory implements IdentityStoreFactory {
    private Map<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>> configMap =
            new HashMap<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore<?>>>();

    public DefaultIdentityStoreFactory() {
        configMap.put(JPAIdentityStoreConfiguration.class, JPAIdentityStore.class);
        configMap.put(LDAPConfiguration.class, LDAPIdentityStore.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IdentityStore<?> createIdentityStore(IdentityStoreConfiguration config, IdentityStoreInvocationContext context) {
        for (Class<? extends IdentityStoreConfiguration> cc : configMap.keySet()) {
            if (cc.isInstance(config)) {
                try {
                    IdentityStore<IdentityStoreConfiguration> store = 
                            (IdentityStore<IdentityStoreConfiguration>) configMap.get(cc).newInstance(); 
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
    public void mapConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
            Class<? extends IdentityStore> storeClass) {
        configMap.put(configClass,  (Class<? extends IdentityStore<?>>) storeClass);
    }

    @Override
    public PartitionStore createTierStore(IdentityStoreConfiguration config) {
        // TODO Auto-generated method stub
        return null;
    }
}
