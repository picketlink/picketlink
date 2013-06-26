package org.picketlink.idm;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.spi.StoreFactory;
//import org.picketlink.idm.internal.ContextualIdentityManager;
//import org.picketlink.idm.internal.DefaultStoreFactory;

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

    /**
     *
     */
    private final EventBridge eventBridge;

    /**
     *
     */
    private final IdGenerator idGenerator;

    /**
     *
     */
    private final StoreFactory storeFactory;

    /**
     *
     */
    private Map<String,IdentityConfiguration> configurations = new ConcurrentHashMap<String,IdentityConfiguration>();

    /**
     * <p>
     * Creates an instance considering all configuration provided by the given {@link IdentityConfiguration}.
     * </p>
     *
     * @param identityConfig
     */
    public PartitionManager(EventBridge eventBridge, IdGenerator idGenerator, StoreFactory storeFactory) {
        LOGGER.identityManagerBootstrapping();

        if (storeFactory == null) {
            this.storeFactory = new DefaultStoreFactory(identityConfig);
        } else {
            this.storeFactory = identityConfig.getStoreFactory();
        }
    }

    public synchronized void addConfiguration(String name, IdentityConfiguration configuration) {
        if (configurations.containsKey(name)) {
            // TODO improve this exception
            throw new RuntimeException("Cannot add configuration " + name +
                    " - a configuration with this name already exists.");
        }
        configurations.put(name, configuration);
    }

    public synchronized void removeConfiguration(String name) {
        configurations.remove(name);
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
            return new ContextualIdentityManager(eventBridge, idGenerator, partition, storeFactory);
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
        // TODO implement
        return null;
    }

    public void addPartition(Partition partition, String configurationName) {
        // TODO implement
    }

    public void updatePartition(Partition partition) {

    }

    public void deletePartition(Partition partition) {

    }

}