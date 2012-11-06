package org.picketlink.idm.config;

import java.util.ArrayList;
import java.util.List;


/**
 * Defines the runtime configuration for Identity Management
 *
 * @author Shane Bryzak
 */
public class IdentityConfiguration {
    private List<IdentityStoreConfiguration> configuredStores = new ArrayList<IdentityStoreConfiguration>();

    public List<IdentityStoreConfiguration> getConfiguredStores() {
        return configuredStores;
    }

    public void addStoreConfiguration(IdentityStoreConfiguration config) {
        configuredStores.add(config);
    }
}
