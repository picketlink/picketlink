package org.picketlink.idm.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The base class for store configurations
 * 
 * @author Shane Bryzak
 */
public abstract class BaseAbstractStoreConfiguration implements StoreConfiguration {

    /**
     * Defines arbitrary property values for the identity store
     */
    private final Map<String,String> properties = new HashMap<String,String>();

    /**
     * Sets a property value
     * 
     * @param name
     * @param value
     */
    @Override
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * Returns the specified property value
     * 
     * @param name
     * @return
     */
    @Override
    public String getPropertyValue(String name) {
        return properties.get(name);
    }
}
