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
package org.picketlink.idm.internal.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;

/**
 * Caches metadata for relationship identity properties, and provides utility methods for working with relationships
 *
 * TODO Make this class thread-safe
 *
 * @author Shane Bryzak
 */
public class RelationshipMetadata {

    /**
     * This Map stores the set of identity properties for each relationship type, so that they are not required to be
     * queried for every single relationship operation.  The properties are populated at runtime.
     */
    private Map<Class<? extends Relationship>, Set<Property<? extends IdentityType>>> relationshipIdentityProperties =
            new ConcurrentHashMap<Class<? extends Relationship>, Set<Property<? extends IdentityType>>>();

    public Set<Partition> getRelationshipPartitions(Relationship relationship) {
        Set<Partition> partitions = new HashSet<Partition>();
        for (Property<? extends IdentityType> prop : getRelationshipIdentityProperties(relationship.getClass())) {
            IdentityType identity = prop.getValue(relationship);
            if (!partitions.contains(identity.getPartition())) {
                partitions.add(identity.getPartition());
            }
        }
        return partitions;
    }

    public Set<Property<? extends IdentityType>> getRelationshipIdentityProperties(
            Class<? extends Relationship> relationshipClass) {

        if (!relationshipIdentityProperties.containsKey(relationshipClass)) {
            ((ConcurrentHashMap<Class<? extends Relationship>, Set<Property<? extends IdentityType>>>)
                    relationshipIdentityProperties).putIfAbsent(relationshipClass,
                    queryRelationshipIdentityProperties(relationshipClass));
        }

        return relationshipIdentityProperties.get(relationshipClass);
    }

    private Set<Property<? extends IdentityType>> queryRelationshipIdentityProperties(Class<? extends Relationship> relationshipClass) {
        PropertyQuery<? extends IdentityType> query = PropertyQueries.createQuery(relationshipClass);
        query.addCriteria(new TypedPropertyCriteria(IdentityType.class));

        Set<Property<? extends IdentityType>> properties = new HashSet<Property<? extends IdentityType>>();
        for (Property<? extends IdentityType> prop : query.getResultList()) {
            properties.add(prop);
        }

        return Collections.unmodifiableSet(properties);
    }
}
