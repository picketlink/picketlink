package org.picketlink.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;


/**
 * 
 * @author Shane Bryzak
 */
public class IdentityManagerProducer {

    // FIXME basic implementation just to get started, we need to rewrite this with proper configuration

    @Inject IdentityCache identityCache;

    @Produces @ApplicationScoped
    public IdentityManager createIdentityManager() {
        IdentityConfiguration identityConfig = new IdentityConfiguration();

        IdentityManager identityManager = new DefaultIdentityManager();
        identityManager.bootstrap(identityConfig, new DefaultIdentityStoreInvocationContextFactory(null, null));
        return identityManager;
    }

}
