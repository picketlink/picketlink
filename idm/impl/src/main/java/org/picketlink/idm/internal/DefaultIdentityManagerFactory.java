package org.picketlink.idm.internal;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
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

    public void setContextFactory(SecurityContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    @Override
    public IdentityManager createIdentityManager() {
        return createIdentityManager(getRealm(Realm.DEFAULT_REALM));
    }

    @Override
    public IdentityManager createIdentityManager(Partition partition) {
        if (partition == null) {
            throw MESSAGES.nullArgument("Partition");
        }

        SecurityContext context = contextFactory.createContext(partition);
        return new DefaultIdentityManager(context, storeFactory);
    }

    @Override
    public Realm getRealm(String id) {
        return storeFactory.getRealm(id);
    }

    @Override
    public Tier getTier(String id) {
        return storeFactory.getTier(id);
    }

}
