package org.picketlink.idm;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration.TypeOperation;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.spi.PartitionStore;
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
    private final Map<String,IdentityConfiguration> configurations;

    /**
     *
     */
    private final Map<Partition,IdentityConfiguration> partitionConfigurations = new ConcurrentHashMap<Partition,IdentityConfiguration>();

    /**
     *
     */
    private final IdentityConfiguration partitionManagementConfig;

    /**
     *
     * @param eventBridge
     * @param idGenerator
     * @param storeFactory
     * @param configurations
     */
    public PartitionManager(EventBridge eventBridge, IdGenerator idGenerator, StoreFactory storeFactory,
            Map<String,IdentityConfiguration> configurations) {
        this(eventBridge, idGenerator, storeFactory, configurations, null);
    }

    /**
     *
     * @param eventBridge
     * @param idGenerator
     * @param storeFactory
     * @param configurations
     * @param partitionManagementConfigName
     */
    public PartitionManager(EventBridge eventBridge, IdGenerator idGenerator, StoreFactory storeFactory,
            Map<String,IdentityConfiguration> configurations, String partitionManagementConfigName) {
        LOGGER.identityManagerBootstrapping();

        this.eventBridge = eventBridge;
        this.idGenerator = idGenerator;
        this.configurations = Collections.unmodifiableMap(configurations);

        if (!StringUtil.isNullOrEmpty(partitionManagementConfigName)) {
            this.partitionManagementConfig = configurations.get(partitionManagementConfigName);
        } else if (configurations.size() == 1) {
            this.partitionManagementConfig = configurations.get(configurations.keySet().iterator().next());
        } else {
            throw new IllegalArgumentException("The partitionManagementConfigName parameter must be specified " +
                    "when more than one configuration has been provided");
        }
    }

    private IdentityConfiguration getConfigurationForPartition(Partition partition) {
        if (partitionConfigurations.containsKey(partition)) {
            return partitionConfigurations.get(partition);
        } else {
            return lookupPartitionConfiguration(partition);
        }
    }

    private synchronized void lookupPartitionConfiguration(Partition partition) {
        PartitionStore store = partitionManagementConfig.getStoreFactory().<PartitionStore<?>>getStoreForType(Partition.class, TypeOperation.read);

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