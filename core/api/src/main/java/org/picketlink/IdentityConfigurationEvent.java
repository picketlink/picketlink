package org.picketlink;

import org.picketlink.idm.config.IdentityConfiguration;

/**
 * TODO - temporary solution 
 * 
 * @author Shane Bryzak
 *
 */
public class IdentityConfigurationEvent {
    private IdentityConfiguration config;
    
    public IdentityConfigurationEvent(IdentityConfiguration config) {
        this.config = config;
    }
    
    public IdentityConfiguration getConfig() {
        return config;
    }
}
