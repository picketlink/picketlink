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
package org.picketlink.idm.file.internal;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.annotations.Stored;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author pedroigor
 */
public class FileCredentialStorage extends AbstractFileType<CredentialStorage> {

    private static final String VERSION = "1";

    protected FileCredentialStorage(CredentialStorage object) {
        super(VERSION, object);
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        for (Property<Serializable> property: getStoredProperties()) {
            Serializable value = property.getValue(getEntry());

            if (value != null) {
                properties.put(property.getName(), value);
            }
        }
    }

    @Override
    protected CredentialStorage doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        CredentialStorage credentialStorage = (CredentialStorage) Class.forName(getType()).newInstance();

        for (Property<Serializable> property: getStoredProperties()) {
            property.setValue(credentialStorage, properties.get(property.getName()));
        }

        return credentialStorage;
    }

    private List<Property<Serializable>> getStoredProperties() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        CredentialStorage credentialStorage = (CredentialStorage) Class.forName(getType()).newInstance();

        PropertyQuery<Serializable> query = PropertyQueries.createQuery(credentialStorage.getClass());

        query.addCriteria(new AnnotatedPropertyCriteria(Stored.class));

        return query.getResultList();
    }
}
