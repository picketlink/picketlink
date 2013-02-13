/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
