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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.common.properties.Property;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;

/**
 * Defines the configuration for a JPA based IdentityStore implementation.
 *
 * @author Shane Bryzak
 *
 */
public class JPAIdentityStoreConfiguration extends BaseAbstractStoreConfiguration {

    /**
     * The identity model definition
     */
    private Map<Class<? extends AttributedType>, IdentityMapping> identityModel =
            new ConcurrentHashMap<Class<? extends AttributedType>, IdentityMapping>();

    private class IdentityMapping {
        private Map<Property, PropertyMapping> properties = new HashMap<Property, PropertyMapping>();

    }

    private class PropertyMapping {

        private Property entityProperty;

    }

    protected JPAIdentityStoreConfiguration(Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures,
            Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships,
            Map<Class<? extends IdentityType>, Set<FeatureOperation>> supportedIdentityTypes, Set<String> realms,
            Set<String> tiers, List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties, List<Class<? extends CredentialHandler>> credentialHandlers) {
        super(supportedFeatures, supportedRelationships, supportedIdentityTypes, realms, tiers, contextInitializers,
                credentialHandlerProperties, credentialHandlers);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void initConfig() {
        // TODO Auto-generated method stub

    }
}
