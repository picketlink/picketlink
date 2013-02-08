package org.picketlink.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.internal.EEIdentityStoreInvocationContextFactory;
import org.picketlink.internal.SecuredIdentityManager;


/**
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentityManagerProducer {
    private IdentityConfiguration identityConfig;

    private IdentityManager identityManager;
    
    @Inject Event<IdentityConfigurationEvent> identityConfigEvent;
    
    @Inject EEIdentityStoreInvocationContextFactory icf;

    @Inject
    public void init() {
        identityConfig = new IdentityConfiguration();
        
        identityConfigEvent.fire(new IdentityConfigurationEvent(identityConfig));

        identityManager = new SecuredIdentityManager(null);

        identityManager.bootstrap(identityConfig, icf);
    }

    @Produces 
    public IdentityManager createIdentityManager() {
        return identityManager;
    }
    
}
