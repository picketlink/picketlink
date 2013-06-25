package org.picketlink.idm;

import java.io.Serializable;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
//import org.picketlink.idm.internal.DefaultIdentityManager;
//import org.picketlink.idm.internal.DefaultSecurityContextFactory;
//import org.picketlink.idm.internal.DefaultStoreFactory;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Realm;
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
public class PartitionManager implements Serializable {

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
    public PartitionManager(IdentityConfiguration identityConfig) {
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
        Realm defaultRealm = getPartition(Realm.class, Realm.DEFAULT_REALM);

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
     * <p>Returns the {@link Partition} with the given name.</p>
     *
     * @param name
     * @return
     */
    public <T extends Partition> T getPartition(Class<T> partitionClass, String name) {
        // FIXME
        return null;
    }

}