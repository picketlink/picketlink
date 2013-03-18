package org.picketlink.idm.internal;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.SecurityContext;
import org.picketlink.idm.spi.SecurityContextFactory;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Default implementation for IdentityManagerFactory
 *
 * @author Shane Bryzak
 *
 */
public class DefaultIdentityManagerFactory implements IdentityManagerFactory {

    private SecurityContextFactory contextFactory;

    private StoreFactory storeFactory;

    private Realm defaultRealm = null;

    public DefaultIdentityManagerFactory(IdentityConfiguration identityConfig) {
        this(identityConfig, new DefaultSecurityContextFactory());
    }

    public DefaultIdentityManagerFactory(IdentityConfiguration identityConfig, SecurityContextFactory contextFactory) {

        this(identityConfig, contextFactory, new DefaultStoreFactory(identityConfig));
    }

    public DefaultIdentityManagerFactory(IdentityConfiguration identityConfig, SecurityContextFactory contextFactory,
            StoreFactory storeFactory) {

        if (identityConfig == null) {
            throw MESSAGES.nullArgument("IdentityConfiguration");
        }

        if (contextFactory == null) {
            throw MESSAGES.nullArgument("IdentityStoreInvocationContextFactory");
        }

        LOGGER.identityManagerBootstrapping();

        this.contextFactory = contextFactory;
        this.storeFactory = storeFactory;

        loadDefaultRealm();
    }

    private Realm getDefaultRealm() {
        if (defaultRealm == null) {
            loadDefaultRealm();
        }
        return defaultRealm;
    }

    private synchronized void loadDefaultRealm() {
        if (defaultRealm == null) {
            // Only attempt to read/create the Realm if one of the configured identity stores support it
            if (storeFactory.isFeatureSupported(null, FeatureGroup.realm, FeatureOperation.create, null)
                    && storeFactory.isFeatureSupported(null, FeatureGroup.realm, FeatureOperation.read, null)) {
                if (defaultRealm == null) {
                    defaultRealm = getRealm(Realm.DEFAULT_REALM);

                    if (defaultRealm == null) {
                        defaultRealm = createRealm(Realm.DEFAULT_REALM);
                    }
                }
            } else {
                // As a last resort create the default realm manually
                defaultRealm = new Realm(Realm.DEFAULT_REALM);
            }
        }
    }

    public void setIdentityStoreFactory(StoreFactory factory) {
        this.storeFactory = factory;
    }

    @Override
    public IdentityManager createIdentityManager() {
        return createIdentityManager(getDefaultRealm());
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        Partition storedPartition = null;

        if (Realm.class.isInstance(partition)) {
            storedPartition = getRealm(partition.getName());
        } else if (Tier.class.isInstance(partition)) {
            storedPartition = getTier(partition.getName());
        }

        if (storedPartition != null) {
            SecurityContext context = contextFactory.createContext(partition);
            return new DefaultIdentityManager(context, storeFactory);
        } else {
            throw MESSAGES.partitionNotFoundWithName(partition.getClass(), partition.getName());
        }
    }

    @Override
    public Realm createRealm(String name) {

        if (name == null) {
            throw MESSAGES.nullArgument("Realm name");
        }

        SecurityContext context = contextFactory.createContext();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.realm, FeatureOperation.create);

        if (store != null) {
            Realm realm = new Realm(name);
            ((PartitionStore) store).createPartition(context, realm);
            return realm;
        } else {
            throw MESSAGES.storeConfigUnsupportedOperation(FeatureGroup.realm, FeatureOperation.create, FeatureGroup.realm,
                    FeatureOperation.create);
        }
    }

    @Override
    public Realm getRealm(String name) {
        SecurityContext context = contextFactory.createContext();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.realm, FeatureOperation.read);

        return store != null ? ((PartitionStore) store).getRealm(context, name) : null;
    }

    @Override
    public void removeRealm(Realm realm) {
        if (realm == null) {
            throw MESSAGES.nullArgument("Realm");
        }

        SecurityContext context = contextFactory.createContext();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.realm, FeatureOperation.delete);

        if (store != null) {
            Realm storedRealm = getRealm(realm.getName());

            if (storedRealm == null) {
                throw MESSAGES.partitionNotFoundWithName(realm.getClass(), realm.getName());
            }

            IdentityQuery<IdentityType> query = createIdentityManager(storedRealm).createIdentityQuery(IdentityType.class);

            query.setParameter(IdentityType.PARTITION, storedRealm);

            if (!query.getResultList().isEmpty()) {
                throw MESSAGES.partitionCouldNotRemoveWithIdentityTypes(storedRealm);
            }

            ((PartitionStore) store).removePartition(context, realm);
        }
    }

    @Override
    public Tier createTier(String name, Tier parent) {
        if (name == null) {
            throw MESSAGES.nullArgument("Tier name");
        }

        if (getTier(name) != null) {
            throw MESSAGES.partitionAlreadyExistsWithName(Tier.class, name);
        }

        SecurityContext context = contextFactory.createContext();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.tier, FeatureOperation.create);

        if (store != null) {
            Tier tier = new Tier(name, parent);

            ((PartitionStore) store).createPartition(context, tier);

            return tier;
        } else {
            throw MESSAGES.storeConfigUnsupportedOperation(FeatureGroup.tier, FeatureOperation.create, FeatureGroup.tier,
                    FeatureOperation.create);
        }
    }

    @Override
    public Tier getTier(String name) {
        SecurityContext context = contextFactory.createContext();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.tier, FeatureOperation.read);

        return store != null ? ((PartitionStore) store).getTier(context, name) : null;
    }

    @Override
    public void removeTier(Tier tier) {
        if (tier == null) {
            throw MESSAGES.nullArgument("Tier");
        }

        SecurityContext context = contextFactory.createContext();

        IdentityStore<?> store = storeFactory.getStoreForFeature(context, FeatureGroup.tier, FeatureOperation.delete);

        if (store != null) {
            Tier storedTier = getTier(tier.getName());

            if (storedTier == null) {
                throw MESSAGES.partitionNotFoundWithName(tier.getClass(), tier.getName());
            }

            IdentityQuery<IdentityType> query = createIdentityManager(storedTier).createIdentityQuery(IdentityType.class);

            query.setParameter(IdentityType.PARTITION, storedTier);

            if (!query.getResultList().isEmpty()) {
                throw MESSAGES.partitionCouldNotRemoveWithIdentityTypes(storedTier);
            }

            ((PartitionStore) store).removePartition(context, tier);
        }
    }

}
