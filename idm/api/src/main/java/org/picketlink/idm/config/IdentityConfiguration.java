package org.picketlink.idm.config;

import java.util.List;


/**
 * Defines the runtime configuration for Identity Management
 *
 * @author Shane Bryzak
 */
public interface IdentityConfiguration {
    List<IdentityStoreConfiguration> getStoreConfiguration();
}
