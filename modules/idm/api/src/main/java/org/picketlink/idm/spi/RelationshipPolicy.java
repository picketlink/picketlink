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

package org.picketlink.idm.spi;

import java.util.Collections;
import java.util.Map;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Relationship;

/**
 * The relationship policy determines how and where relationships between identities are stored.
 *
 * There is a one-to-one relationship between an IdentityConfiguration and RelationshipPolicy, i.e. each
 * IdentityConfiguration has its own RelationshipPolicy.
 *
 * The RelationshipPolicy defines the relationship types that are managed by the IdentityConfiguration itself for
 * "self-contained" relationships, i.e. relationships for which all participating identities belong to a partition
 * governed by that IdentityConfiguration.  It also defines the "global" (i.e. relationships between identities
 * belonging to different partitions/configurations) relationship types that the IdentityConfiguration
 * is capable of supporting.
 *
 * It is the responsibility of the developer to ensure that multiple RelationshipPolicy configurations do not provide
 * overlapping support for the same global relationship types.  If multiple IdentityConfiguration instances have been
 * configured and their relationship policies provide overlapping support, the behaviour is undefined.
 *
 * @author Shane Bryzak
 */
public class RelationshipPolicy {

    private Map<Class<? extends Relationship>, IdentityStoreConfiguration> selfManagedRelationships;

    private Map<Class<? extends Relationship>, IdentityStoreConfiguration> globalManagedRelationships;

    public RelationshipPolicy(Map<Class<? extends Relationship>, IdentityStoreConfiguration> selfManagedRelationships,
            Map<Class<? extends Relationship>, IdentityStoreConfiguration> globalManagedRelationships) {
        this.selfManagedRelationships = Collections.unmodifiableMap(selfManagedRelationships);
        this.globalManagedRelationships = Collections.unmodifiableMap(globalManagedRelationships);
    }

    public boolean isSelfRelationshipSupported(Class<? extends Relationship> relationshipClass) {
        return selfManagedRelationships.containsKey(relationshipClass);
    }

    public IdentityStoreConfiguration getSelfManagedRelationshipStore(Class<? extends Relationship> relationshipClass) {
        return selfManagedRelationships.get(relationshipClass);
    }

    public boolean isGlobalRelationshipSupported(Class<? extends Relationship> relationshipClass) {
        return globalManagedRelationships.containsKey(relationshipClass);
    }

    public IdentityStoreConfiguration getGlobalManagedRelationshipStore(Class<? extends Relationship> relationshipClass) {
        return globalManagedRelationships.get(relationshipClass);
    }
}
