package org.picketlink.idm.jpa.schema.internal;

import java.util.HashSet;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;

/**
 *
 */
public class SimpleJPAIdentityStoreConfiguration extends IdentityStoreConfiguration {
    
    private Set<Feature> featureSet = new HashSet<IdentityStore.Feature>();
    
    @Override
    public void init() throws SecurityConfigurationException {
        this.featureSet.add(Feature.all);
    }

    @Override
    public Set<Feature> getFeatureSet() {
        return this.featureSet;
    }

}
