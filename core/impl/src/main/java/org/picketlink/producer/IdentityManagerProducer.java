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
@ApplicationScoped
public class IdentityManagerProducer {

    private IdentityConfiguration identityConfig;
    
    private IdentityManager identityManager;
    
    // FIXME basic implementation just to get started, we need to rewrite this with proper configuration

   // @Inject IdentityCache identityCache;
    
    public IdentityManagerProducer() {
        identityConfig = new IdentityConfiguration();
        
        identityManager = new SecuredIdentityManager();
        
        identityManager.bootstrap(identityConfig, new DefaultIdentityStoreInvocationContextFactory(null, null));
    }

    @Produces @ApplicationScoped
    public IdentityManager createIdentityManager() {
        return identityManager;
    }

}
