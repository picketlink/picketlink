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

package org.picketlink.config.idm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Type representing IdentityStoreConfiguration or PartitionStoreConfiguration
 *
 * TODO: Move this class to config module. For now it needs to be in federation because needs to be accessible from PicketlinkType class
 * TODO: Add XML config snippet similarly like for other type classes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class StoreConfigurationType {

    private String className;
    private Map<String, Object> configProperties = new HashMap<String, Object>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, Object> getAllProperties() {
        return Collections.unmodifiableMap(configProperties);
    }

    public Object getProperty(String propertyName) {
        return configProperties.get(propertyName);
    }

    public void addProperty(String propertyName, Object propertyValue) {
        configProperties.put(propertyName, propertyValue);
    }
}
