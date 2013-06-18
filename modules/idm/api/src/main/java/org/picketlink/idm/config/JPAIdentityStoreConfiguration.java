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
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
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
     * The identity model definition, which maps between specific identity types
     * and their identity mapping metadata
     */
    private Map<Class<? extends IdentityType>, ModelDefinition> identityModel =
            new ConcurrentHashMap<Class<? extends IdentityType>, ModelDefinition>();

    /**
     * Credential model definition
     */
    private Map<Class<? extends CredentialStorage>, ModelDefinition> credentialModel;

    /**
     *
     */
    private RelationshipModel relationshipModel;

    /**
     *
     */
    private Map<Class<? extends Partition>, ModelDefinition> partitionModel = 
            new HashMap<Class<? extends Partition>, ModelDefinition>();

    /**
     * Each model definition maps between a Property of the identity model and its corresponding
     * entity bean property, and also maps ad-hoc attribute schemas to the identity type.
     */
    private class ModelDefinition {
        /**
         * Maps between a property of an identity model class and its corresponding entity bean property
         */
        private Map<Property, PropertyMapping> properties = new HashMap<Property, PropertyMapping>();

        /**
         * Ad-hoc attribute values
         */
        private Map<Class<?>, AttributeMapping> attributes = new HashMap<Class<?>, AttributeMapping>();
    }

    private class RelationshipModel {
        private Class<?> relationshipClass;
        private Class<?> relationshipIdentityClass;
        private Map<Class<?>, AttributeMapping> attributes = new HashMap<Class<?>, AttributeMapping>();
    }

    private class PropertyMapping {

        private Property entityProperty;

    }

    private class AttributeMapping {
        private PropertyMapping ownerReference;
        private Property<String> attributeName;
        private Property<String> attributeClass;
        private Property<Object> attributeValue;
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
