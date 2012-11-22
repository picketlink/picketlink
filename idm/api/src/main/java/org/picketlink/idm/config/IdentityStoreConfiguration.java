/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.idm.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStore.Feature;

/**
 * Represents a configuration for {@link IdentityStore}
 *
 * @author Anil Saldhana
 * @author Shane Bryzak
 * @since Sep 6, 2012
 */
public abstract class IdentityStoreConfiguration {

    /**
     * Defines the realm supported by this identity store.  If no realm is specified, then
     * this identity store will be used during all supported operations where the selected
     * realm is not explicitly served by a different identity store.
     */
    private String realm;

    /**
     * Defines arbitrary property values for the identity store
     */
    private final Map<String,String> properties = new HashMap<String,String>();

    /**
     * Metadata reflecting which features are supported by this identity store
     */
    private final Set<Feature> supportedFeatures = new HashSet<Feature>();

    /**
     * Returns the realm for this identity store
     * 
     * @return
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Sets the realm for this identity store
     * 
     * @param realm
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Returns a Set describing the features supported by this identity store
     * 
     * @return
     */
    public Set<Feature> getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * Adds the specified feature to the supported features for this identity store
     * 
     * @param feature
     */
    public void addSupportedFeature(Feature feature) {
        supportedFeatures.add(feature);
    }

    /**
     * Removes the specified feature from the supported features for this identity store
     * @param feature
     */
    public void removeSupportedFeature(Feature feature) {
        supportedFeatures.remove(feature);
    }

    /**
     * Sets a property value
     * 
     * @param name
     * @param value
     */
    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    /**
     * Returns the specified property value
     * 
     * @param name
     * @return
     */
    public String getPropertyValue(String name) {
        return properties.get(name);
    }
}