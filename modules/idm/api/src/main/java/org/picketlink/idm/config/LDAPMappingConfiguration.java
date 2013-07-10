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

import java.util.Map;
import java.util.Set;
import org.picketlink.idm.model.AttributedType;

/**
 * @author pedroigor
 */
public class LDAPMappingConfiguration {

    private Class<? extends AttributedType> mappedClass;
    private Set<String> objectClasses;
    private String baseDN;
    private String idAttributeName;
    private Map<String, String> mappedProperties;

    LDAPMappingConfiguration(Class<? extends AttributedType> mappedClass,
                             Set<String> objectClasses,
                             String baseDN,
                             String idAttributeName,
                             Map<String, String> mappedProperties) {
        this.mappedClass = mappedClass;
        this.objectClasses = objectClasses;
        this.baseDN = baseDN;
        this.idAttributeName = idAttributeName;
        this.mappedProperties = mappedProperties;
    }

    public Class<? extends AttributedType> getMappedClass() {
        return this.mappedClass;
    }

    public void setMappedClass(Class<? extends AttributedType> mappedClass) {
        this.mappedClass = mappedClass;
    }

    public Set<String> getObjectClasses() {
        return this.objectClasses;
    }

    public void setObjectClasses(Set<String> objectClasses) {
        this.objectClasses = objectClasses;
    }

    public String getBaseDN() {
        return this.baseDN;
    }

    public void setBaseDN(String baseDN) {
        this.baseDN = baseDN;
    }

    public String getIdAttributeName() {
        return this.idAttributeName;
    }

    public void setIdAttributeName(String idAttributeName) {
        this.idAttributeName = idAttributeName;
    }

    public Map<String, String> getMappedProperties() {
        return this.mappedProperties;
    }

    public void setMappedProperties(Map<String, String> mappedProperties) {
        this.mappedProperties = mappedProperties;
    }
}
