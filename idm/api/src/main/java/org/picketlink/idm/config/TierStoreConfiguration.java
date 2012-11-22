package org.picketlink.idm.config;

/**
 * Defines a configuration for a TierStore
 * 
 * @author Shane Bryzak
 *
 */
public class TierStoreConfiguration {
    private Class<?> tierClass;

    public Class<?> getTierClass() {
        return tierClass;
    }

    public void setTierClass(Class<?> tierClass) {
        this.tierClass = tierClass;
    }
}
