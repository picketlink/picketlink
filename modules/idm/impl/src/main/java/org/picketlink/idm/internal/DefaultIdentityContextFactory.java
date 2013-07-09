package org.picketlink.idm.internal;

import org.picketlink.idm.IdentityContext;
import org.picketlink.idm.IdentityContextFactory;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.spi.SecurityContextFactory;

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class DefaultIdentityContextFactory implements IdentityContextFactory {
    protected SecurityContextFactory contextFactory;
    protected DefaultStoreFactory2 storeFactory;
    protected IdentityConfiguration identityConfig;

    public DefaultIdentityContextFactory(IdentityConfiguration identityConfig) {
        LOGGER.identityManagerBootstrapping();

        this.identityConfig = identityConfig;
        if (identityConfig == null) {
            throw MESSAGES.nullArgument("IdentityConfiguration");
        }

        if (contextFactory == null) {
            this.contextFactory = new DefaultSecurityContextFactory();
        } else {
            this.contextFactory = identityConfig.getSecurityContextFactory();
        }

        if (storeFactory == null) {
            this.storeFactory = new DefaultStoreFactory2(identityConfig);
        } else {
            throw new RuntimeException("not supported");
        }
        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {
            if (config.getIdentityContextHandler() != null) {
                config.getIdentityContextHandler().initialize();
            }
        }
    }


    @Override
    public IdentityContext createIdentityContext() {
        return new DefaultIdentityContext(identityConfig, contextFactory, storeFactory);
    }

    @Override
    public void close() {
        for (IdentityStoreConfiguration config : identityConfig.getConfiguredStores()) {
            if (config.getIdentityContextHandler() != null) {
                config.getIdentityContextHandler().close();
            }
        }

    }
}
