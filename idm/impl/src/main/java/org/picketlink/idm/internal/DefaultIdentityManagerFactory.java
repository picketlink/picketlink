package org.picketlink.idm.internal;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
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
    }

    public void setIdentityStoreFactory(StoreFactory factory) {
        this.storeFactory = factory;
    }

    @Override
    public IdentityManager createIdentityManager() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) {
        SecurityContext context = contextFactory.createContext(partition);
        return new DefaultIdentityManager(context, storeFactory);
    }

    @Override
    public Realm createRealm(String name) {

        if (name == null) {
            throw MESSAGES.nullArgument("Realm name");
        }

        IdentityStore<?> store = storeFactory.getStoreForFeature(contextFactory.createContext(null),
                FeatureGroup.realm, FeatureOperation.create);

        if (store != null) {
            Realm realm = new Realm(name);
            ((PartitionStore) store).createPartition(contextFactory.createContext(null), realm);
            return realm;
        } else {
            throw MESSAGES.storeConfigUnsupportedOperation(FeatureGroup.realm, FeatureOperation.create,
                    FeatureGroup.realm, FeatureOperation.create);
        }
    }

    @Override
    public Realm getRealm(String name) {
        IdentityStore<?> store = storeFactory.getStoreForFeature(contextFactory.createContext(null),
                FeatureGroup.realm, FeatureOperation.read);

        return store != null ? ((PartitionStore) store).getRealm(contextFactory.createContext(null), name) : null;
    }

    @Override
    public void removeRealm(Realm realm) {
        if (realm == null) {
            throw MESSAGES.nullArgument("Realm");
        }

        IdentityStore<?> store = storeFactory.getStoreForFeature(contextFactory.createContext(null),
                FeatureGroup.realm, FeatureOperation.delete);

        if (store != null) {
            ((PartitionStore) store).removePartition(contextFactory.createContext(null), realm);
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

        IdentityStore<?> store = storeFactory.getStoreForFeature(contextFactory.createContext(null),
                FeatureGroup.tier, FeatureOperation.create);

        if (store != null) {
            Tier tier = new Tier(name, parent);

            ((PartitionStore) store).createPartition(contextFactory.createContext(null), tier);

            return tier;
        } else {
            throw MESSAGES.storeConfigUnsupportedOperation(FeatureGroup.tier, FeatureOperation.create,
                    FeatureGroup.tier, FeatureOperation.create);
        }
    }

    @Override
    public Tier getTier(String name) {
        IdentityStore<?> store = storeFactory.getStoreForFeature(contextFactory.createContext(null),
                FeatureGroup.tier, FeatureOperation.read);

        return store != null ? ((PartitionStore) store).getTier(contextFactory.createContext(null), name) : null;
    }

    @Override
    public void removeTier(Tier tier) {
        if (tier == null) {
            throw MESSAGES.nullArgument("Tier");
        }

        IdentityStore<?> store = storeFactory.getStoreForFeature(contextFactory.createContext(null),
                FeatureGroup.tier, FeatureOperation.delete);

        if (store != null) {
            ((PartitionStore) store).removePartition(contextFactory.createContext(null), tier);
        }
    }

}
