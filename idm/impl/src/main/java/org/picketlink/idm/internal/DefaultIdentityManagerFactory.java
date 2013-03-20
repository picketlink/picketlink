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

    @Override
    public IdentityManager createIdentityManager() {
        // FIXME
        return createIdentityManager(null);
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
    public Realm getRealm(String name) {
        // fIXME
        return null;
    }

    @Override
    public Tier getTier(String name) {
        // FIXME
        return null;
    }

}
