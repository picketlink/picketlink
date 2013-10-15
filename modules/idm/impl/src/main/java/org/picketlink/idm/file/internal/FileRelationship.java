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
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.internal.RelationshipReference;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.Entry;
import static org.picketlink.common.properties.query.TypedPropertyCriteria.MatchOption;

/**
 * @author pedroigor
 */
public class FileRelationship extends AbstractFileAttributedType<Relationship> {

    private static final long serialVersionUID = -507972683694827934L;

    private static final transient String FILE_RELATIONSHIP_VERSION = "1";

    private Map<String, String> identityTypeIds = new HashMap<String, String>();

    protected FileRelationship(Relationship object) {
        super(FILE_RELATIONSHIP_VERSION, object);
    }

    @Override
    protected Relationship doCreateInstance(Map<String, Serializable> properties) throws Exception {
        return (Relationship) Class.forName(getType()).newInstance();
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);

        List<Property<IdentityType>> relationshipIdentityTypes = PropertyQueries
                .<IdentityType> createQuery(getEntry().getClass())
                .addCriteria(new TypedPropertyCriteria(IdentityType.class, MatchOption.SUB_TYPE)).getResultList();

        for (Property<IdentityType> annotatedProperty : relationshipIdentityTypes) {
            IdentityType identityType = annotatedProperty.getValue(getEntry());

            if (identityType != null) {
                this.identityTypeIds.put(RelationshipReference.formatId(identityType), annotatedProperty.getName());
            }
        }
    }

    @Override
    protected void doWriteObject(ObjectOutputStream s) throws Exception {
        s.writeObject(this.identityTypeIds);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doReadObject(ObjectInputStream s) throws Exception {
        this.identityTypeIds = (Map<String, String>) s.readObject();
    }

    @Override
    protected Relationship doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        Relationship relationship = super.doPopulateEntry(properties);

        List<Property<Serializable>> relationshipAttributeTypes = PropertyQueries
                .<Serializable> createQuery(relationship.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

        for (Property<Serializable> property : relationshipAttributeTypes) {
            property.setValue(relationship, properties.get(property.getName()));
        }

        return relationship;
    }

    public String getIdentityTypeId(String roleName) {
        Set<Entry<String, String>> entrySet = this.identityTypeIds.entrySet();

        for (Entry<String, String> entry : entrySet) {
            if (roleName.equals(entry.getValue())) {
                return entry.getKey();
            }
        }

        return null;
    }

    public boolean hasIdentityType(IdentityType identityType) {
        return this.identityTypeIds.containsKey(RelationshipReference.formatId(identityType));
    }

}
