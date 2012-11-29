package org.picketlink.idm.config;

import org.picketlink.idm.SecurityConfigurationException;

/**
 * This interface defines the basic methods required for a store configuration 
 *  
 * @author Shane Bryzak
 */
public interface StoreConfiguration {
    /**
     * 
     * @param name
     * @param value
     */
    void setProperty(String name, String value);

    /**
     * 
     * @param name
     * @return
     */
    String getPropertyValue(String name);

    /**
     * Initializes the store configuration
     * 
     * @throws SecurityConfigurationException
     */
    void init() throws SecurityConfigurationException;
}
