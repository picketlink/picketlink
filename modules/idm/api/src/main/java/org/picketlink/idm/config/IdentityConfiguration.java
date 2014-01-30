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

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.GroupRole;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.spi.RelationshipPolicy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;

/**
 * <p>Consolidates all the configuration that should be used to initialize and start the IDM subsystem.</p>
 *
 * <p>Each configuration have a name, and they must be unique when using multiple configurations.</p>
 *
 * @author Shane Bryzak
 */
public class IdentityConfiguration {

    @SuppressWarnings("rawtypes")
    private static final Class[] DEFAULT_IDENTITY_TYPES = {
        Agent.class,
        User.class,
        Group.class,
        Role.class
    };

    @SuppressWarnings("rawtypes")
    private static final Class[] DEFAULT_RELATIONSHIP_TYPES = {
        Grant.class,
        GroupMembership.class,
        GroupRole.class
    };

    private final String name;
    private final List<? extends IdentityStoreConfiguration> storeConfiguration;
    private final RelationshipPolicy relationshipPolicy;

    private final Set<Class<? extends IdentityType>> registeredIdentityTypes;
    private final Set<Class<? extends Relationship>> registeredRelationshipTypes;

    IdentityConfiguration(String name, List<? extends IdentityStoreConfiguration> identityStores,
            RelationshipPolicy relationshipPolicy) {
        this(name, identityStores, relationshipPolicy, null, null);
    }

    @SuppressWarnings("unchecked")
    IdentityConfiguration(String name, List<? extends IdentityStoreConfiguration> identityStores,
            RelationshipPolicy relationshipPolicy, Set<Class<? extends IdentityType>> registeredIdentityTypes,
            Set<Class<? extends Relationship>> registeredRelationshipTypes) {
        if (name == null) {
            throw new SecurityConfigurationException("You must specify a name for the IdentityConfiguration.");
        }

        this.name = name;
        this.storeConfiguration = unmodifiableList(identityStores);
        this.relationshipPolicy = relationshipPolicy;

        this.registeredIdentityTypes = registeredIdentityTypes.isEmpty() || registeredIdentityTypes == null ?
                 new HashSet<Class<? extends IdentityType>>(Arrays.<Class<? extends IdentityType>>asList(DEFAULT_IDENTITY_TYPES))
                 : registeredIdentityTypes;

        this.registeredRelationshipTypes = registeredRelationshipTypes.isEmpty() || registeredRelationshipTypes == null ?
                new HashSet<Class<? extends Relationship>>(Arrays.<Class<? extends Relationship>>asList(DEFAULT_RELATIONSHIP_TYPES))
                : registeredRelationshipTypes;
    }

    public RelationshipPolicy getRelationshipPolicy() {
        return relationshipPolicy;
    }

    public String getName() {
        return this.name;
    }

    public List<? extends IdentityStoreConfiguration> getStoreConfiguration() {
        return this.storeConfiguration;
    }

    public boolean supportsPartition() {
        for (IdentityStoreConfiguration storeConfiguration : getStoreConfiguration()) {
            if (storeConfiguration.supportsPartition()) {
                return true;
            }
        }

        return false;
    }

    public Set<Class<? extends IdentityType>> getRegisteredIdentityTypes() {
        return registeredIdentityTypes;
    }

    public Set<Class<? extends Relationship>> getRegisteredRelationshipTypes() {
        return registeredRelationshipTypes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        IdentityConfiguration other = (IdentityConfiguration) obj;

        return getName() != null && other.getName() != null && getName().equals(other.getName());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * <p>Check if the configuration supports credential management.</p>
     *
     * <p>Credential management is supported if any of the configured identity stores support it.</p>
     *
     * @return True if the configuration supports credential. Otherwise is false.
     */
    public boolean supportsCredential() {
        for (IdentityStoreConfiguration storeConfiguration: getStoreConfiguration()) {
            if (storeConfiguration.supportsCredential()) {
                return true;
            }
        }

        return false;
    }
}