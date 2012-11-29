package org.picketlink.idm.config;

import java.util.ArrayList;
import java.util.List;


/**
 * Defines the runtime configuration for Identity Management
 *
 * @author Shane Bryzak
 */
public class IdentityConfiguration {
    private List<StoreConfiguration> configuredStores = new ArrayList<StoreConfiguration>();

    public List<StoreConfiguration> getConfiguredStores() {
        return configuredStores;
    }

    public void addStoreConfiguration(StoreConfiguration config) {
        configuredStores.add(config);
    }
}
