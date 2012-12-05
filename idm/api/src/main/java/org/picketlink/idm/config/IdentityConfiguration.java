package org.picketlink.idm.config;

import java.util.ArrayList;
import java.util.List;

import org.picketlink.idm.credential.spi.CredentialHandlerFactory;


/**
 * Defines the runtime configuration for Identity Management
 *
 * @author Shane Bryzak
 */
public class IdentityConfiguration {
    private List<StoreConfiguration> configuredStores = new ArrayList<StoreConfiguration>();

    private CredentialHandlerFactory credentialHandlerFactory;

    public List<StoreConfiguration> getConfiguredStores() {
        return configuredStores;
    }

    public void addStoreConfiguration(StoreConfiguration config) {
        configuredStores.add(config);
    }

    public CredentialHandlerFactory getCredentialHandlerFactory() {
        return credentialHandlerFactory;
    }

    public void setCredentialHandlerFactory(CredentialHandlerFactory factory) {
        this.credentialHandlerFactory = factory;
    }
}
