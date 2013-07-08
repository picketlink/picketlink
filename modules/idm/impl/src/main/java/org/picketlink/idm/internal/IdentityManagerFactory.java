package org.picketlink.idm.internal;

import java.io.Serializable;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.SecurityContextFactory;
import org.picketlink.idm.spi.StoreFactory;
import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

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
public class IdentityManagerFactory implements Serializable {

    private static final long serialVersionUID = 666601082732493295L;

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
        LOGGER.identityManagerBootstrapping();

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

    /**
     *
     *
     * @param name
     * @return null if Realm exists
     */
    public Realm createRealm(String name) {
        SecurityContext context = contextFactory.createContext(null);
        return this.storeFactory.createRealm(context, name);
    }

    /**
     *
     *
     * @param name
     * @return null if Realm exists
     */
    public Tier createTier(String name) {
        SecurityContext context = contextFactory.createContext(null);
        return this.storeFactory.createTier(context, name);
    }

    /**
     *
     * @param name
     * @return null if realm does not exist
     */
    public Realm findRealm(String name) {
        SecurityContext context = contextFactory.createContext(null);
        return this.storeFactory.findRealm(context, name);
    }

    /**
     *
     * @param name
     * @return null if realm does not exist
     */
    public Tier findTier(String name) {
        SecurityContext context = contextFactory.createContext(null);
        return this.storeFactory.findTier(context, name);
    }

    public void deleteRealm(Realm realm) {
        if (findRealm(realm.getId()) == null) return;
        this.storeFactory.deleteRealm(contextFactory.createContext(realm), realm);

    }

   public void deleteTier(Tier tier) {
      if (findTier(tier.getId()) == null) return;
      this.storeFactory.deleteTier(contextFactory.createContext(tier), tier);

   }

}