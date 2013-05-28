package org.picketlink.idm.internal;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.SecurityContextFactory;
import org.picketlink.idm.spi.StoreFactory;

/**
 * <p>
 * Factory class for {@link IdentityManager} instances.
 * </p>
 * <p>
 * Before using this factory you need a valid {@link IdentityConfiguration}, usually created using the
 * {@link org.picketlink.idm.config.IdentityConfigurationBuilder}.
 * </p>
 *
 * @author Shane Bryzak
 */
public class IdentityManagerFactory {

    private SecurityContextFactory contextFactory;
    private StoreFactory storeFactory;

    /**
     * <p>
     * Creates an instance considering all configuration provided by the given {@link IdentityConfiguration}.
     * </p>
     *
     * @param identityConfig
     */
    public IdentityManagerFactory(IdentityConfiguration identityConfig) {
        if (identityConfig == null) {
            throw MESSAGES.nullArgument("IdentityConfiguration");
        }

        if (contextFactory == null) {
            this.contextFactory = new DefaultSecurityContextFactory();
        } else {
            this.contextFactory = identityConfig.getSecurityContextFactory();
        }

        if (storeFactory == null) {
            this.storeFactory = new DefaultStoreFactory(identityConfig);
        } else {
            this.storeFactory = identityConfig.getStoreFactory();
        }

        if (identityConfig.getIdentityStores() != null) {
            Set<Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>>> entrySet = identityConfig.getIdentityStores().entrySet();

            for (Entry<Class<? extends IdentityStoreConfiguration>, Class<? extends IdentityStore>> entry : entrySet) {
                this.storeFactory.mapIdentityConfiguration(entry.getKey(), (Class<? extends IdentityStore<?>>) entry.getValue());
            }
        }

        LOGGER.identityManagerBootstrapping();
    }

    /**
     * <p>
     * Creates a {@link IdentityManager} instance using the default realm (<code>Realm.DEFAULT_REALM</code>).
     * </p>
     *
     * @return
     * @throws SecurityConfigurationException if the default realm was not configured.
     */
    public IdentityManager createIdentityManager() throws SecurityConfigurationException{
        Realm defaultRealm = getRealm(Realm.DEFAULT_REALM);

        if (defaultRealm == null) {
            throw MESSAGES.configurationDefaultRealmNotDefined();
        }

        return createIdentityManager(defaultRealm);
    }

    /**
     * <p>
     * Creates a {@link IdentityManager} instance for the given {@link Partition}.
     * </p>
     *
     * @param partition
     * @return
     * @throws SecurityConfigurationException if the default realm was not configured.
     * @throws IdentityManagementException if provided a null partition or some error occurs during the creation..
     */
    public IdentityManager createIdentityManager(Partition partition) throws SecurityConfigurationException, IdentityManagementException {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        try {
            SecurityContext context = contextFactory.createContext(partition);

            return new DefaultIdentityManager(context, storeFactory);
        } catch (Exception e) {
            throw MESSAGES.couldNotCreateContextualIdentityManager(partition);
        }
    }

    /**
     * <p>Returns a {@link Realm} configured with the given name.</p>
     *
     * @param name
     * @return
     */
    public Realm getRealm(String name) {
        return this.storeFactory.getRealm(name);
    }

    /**
     * <p>Returns a {@link Tier} configured with the given name.</p>
     *
     * @param name
     * @return
     */
    public Tier getTier(String name) {
        return this.storeFactory.getTier(name);
    }

}