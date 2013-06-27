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
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * Provides partition management functionality, and partition-specific {@link IdentityManager} instances.
 *
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

    public PartitionManager(Map<String,IdentityConfiguration> configurations) {
        this(configurations,
             new EventBridge() { public void raiseEvent(Object event) { /* no-op */}},
             new DefaultIdGenerator());
    }

    /**
     *
     * @param eventBridge
     * @param idGenerator
     * @param storeFactory
     * @param configurations
     */
    public PartitionManager(Map<String,IdentityConfiguration> configurations, EventBridge eventBridge, IdGenerator idGenerator) {
        this(configurations, eventBridge, idGenerator, null);
    }

    /**
     *
     * @param eventBridge
     * @param idGenerator
     * @param storeFactory
     * @param configurations
     * @param partitionManagementConfigName
     */
    public PartitionManager(Map<String,IdentityConfiguration> configurations, EventBridge eventBridge, IdGenerator idGenerator,
            String partitionManagementConfigName) {
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

    private synchronized IdentityConfiguration lookupPartitionConfiguration(Partition partition) {
        if (!partitionConfigurations.containsKey(partition)) {

            @SuppressWarnings("rawtypes")
            PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                    createIdentityContext(), Partition.class, TypeOperation.read);

            partitionConfigurations.put(partition, configurations.get(store.getConfigurationName(partition)));
        }
        return partitionConfigurations.get(partition);
    }

    private IdentityContext createIdentityContext() {
        // TODO implement this
        return null;
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
            return new ContextualIdentityManager(eventBridge, idGenerator, partition);
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
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.read);
        return store.<T>getPartition(partitionClass, name);
    }

    /**
     * Persists the specified partition, using the provided configuration name
     *
     * @param partition
     * @param configurationName
     */
    public void addPartition(Partition partition, String configurationName) {
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.create);
        store.addPartition(partition, configurationName);
    }

    /**
     * Updates the state of the specified partition.  The partition ID cannot be changed.
     *
     * @param partition
     */
    public void updatePartition(Partition partition) {
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.update);
        store.updatePartition(partition);
    }

    /**
     * Removes the specified partition
     *
     * @param partition
     */
    public void removePartition(Partition partition) {
        PartitionStore<?> store = partitionManagementConfig.getStoreFactory().<PartitionStore>getStoreForType(PartitionStore.class,
                createIdentityContext(), Partition.class, TypeOperation.delete);
        store.removePartition(partition);
    }

}