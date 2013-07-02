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

import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.model.Relationship;

/**
 * The relationship policy determines how and where relationships between identities are stored.
 * Each PartitionManager instance has its own RelationshipPolicy
 *
 * @author Shane Bryzak
 *
 */
public class RelationshipPolicy {
    private Map<Class<? extends Relationship>, IdentityConfiguration> relationshipConfigs;

    public RelationshipPolicy(Map<Class<? extends Relationship>, IdentityConfiguration> relationshipConfigs) {
        this.relationshipConfigs = Collections.unmodifiableMap(relationshipConfigs);
    }

    public IdentityConfiguration getGlobalRelationshipConfig(Class<? extends Relationship> relationshipClass) {
        return relationshipConfigs.get(relationshipClass);
    }
}
