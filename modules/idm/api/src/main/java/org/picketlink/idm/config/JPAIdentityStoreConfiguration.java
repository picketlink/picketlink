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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.jpa.annotations.AttributeClass;
import org.picketlink.idm.jpa.annotations.AttributeName;
import org.picketlink.idm.jpa.annotations.AttributeValue;
import org.picketlink.idm.jpa.annotations.Identifier;
import org.picketlink.idm.jpa.annotations.OwnerReference;
import org.picketlink.idm.jpa.annotations.RelationshipClass;
import org.picketlink.idm.jpa.annotations.RelationshipDescriptor;
import org.picketlink.idm.jpa.annotations.RelationshipMember;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.ContextInitializer;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Defines the configuration for a JPA based IdentityStore implementation.
 *
 * @author Shane Bryzak
 */
public class JPAIdentityStoreConfiguration extends AbstractIdentityStoreConfiguration {

    private final List<Class<?>> entityTypes;
    private final AttributeMapping attributeMapping;
    private final RelationshipIdentityMapping relationshipIdentityMapping;
    private final RelationshipMapping relationshipMapping;

    protected JPAIdentityStoreConfiguration(
            List<Class<?>> entityTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes,
            List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties,
            List<Class<? extends CredentialHandler>> credentialHandlers) {
        super(supportedTypes, unsupportedTypes, contextInitializers,credentialHandlerProperties, credentialHandlers);

        if (entityTypes == null) {
            throw MESSAGES.jpaConfigNoEntityClassesProvided();
        }
        
        this.entityTypes = entityTypes;

        AttributeMapping attributeMapping = null;
        RelationshipMapping relationshipMapping = null;
        RelationshipIdentityMapping relationshipIdentityMapping = null;

        for (Class<?> entityType: this.entityTypes) {
            Property<Object> property = PropertyQueries
                    .createQuery(entityType)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeClass.class))
                    .getFirstResult();

            if (property != null) {
                attributeMapping = new AttributeMapping(entityType);
            }

            property = PropertyQueries
                    .createQuery(entityType)
                    .addCriteria(new AnnotatedPropertyCriteria(RelationshipClass.class))
                    .getFirstResult();

            if (property != null) {
                relationshipMapping = new RelationshipMapping(entityType);
            }

            property = PropertyQueries
                    .createQuery(entityType)
                    .addCriteria(new AnnotatedPropertyCriteria(RelationshipDescriptor.class))
                    .getFirstResult();

            if (property != null) {
                relationshipIdentityMapping = new RelationshipIdentityMapping(entityType);
            }
        }

        this.attributeMapping = attributeMapping;
        this.relationshipMapping = relationshipMapping;
        this.relationshipIdentityMapping = relationshipIdentityMapping;
    }

    @Override
    protected void initConfig() {  }

    public List<Class<?>> getEntityTypes() {
        return this.entityTypes;
    }

    public static class AttributeMapping {
        private final Class<?> entityClass;
        private final Property<String> attributeName;
        private final Property<String> attributeClass;
        private final Property<Object> attributeValue;
        private final Property<Object> attributeOwner;

        private AttributeMapping(Class<?> entityClass) {
            this.entityClass = entityClass;
            this.attributeClass = PropertyQueries
                    .<String>createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeClass.class))
                    .getSingleResult();
            this.attributeName = PropertyQueries
                    .<String>createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeName.class))
                    .getSingleResult();
            this.attributeValue = PropertyQueries
                    .createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(AttributeValue.class))
                    .getSingleResult();
            this.attributeOwner = PropertyQueries
                    .createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                    .getSingleResult();
        }

        public Class<?> getEntityClass() {
            return this.entityClass;
        }

        public Property<String> getAttributeName() {
            return this.attributeName;
        }

        public Property<String> getAttributeClass() {
            return this.attributeClass;
        }

        public Property<Object> getAttributeValue() {
            return this.attributeValue;
        }

        public Property<Object> getAttributeOwner() {
            return this.attributeOwner;
        }
    }

    public static class RelationshipMapping {
        private final Class<?> entityClass;
        private final Property<String> relationshipIdentifier;
        private final Property<String> relationshipClass;

        private RelationshipMapping(Class<?> entityClass) {
            this.entityClass = entityClass;
            this.relationshipIdentifier = PropertyQueries
                    .<String>createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(Identifier.class))
                    .getSingleResult();
            this.relationshipClass = PropertyQueries
                    .<String>createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(RelationshipClass.class))
                    .getSingleResult();
        }

        public Class<?> getEntityClass() {
            return this.entityClass;
        }

        public Property<String> getRelationshipIdentifier() {
            return relationshipIdentifier;
        }

        public Property<String> getRelationshipClass() {
            return relationshipClass;
        }
    }

    public static class RelationshipIdentityMapping {
        private final Class<?> entityClass;
        private final Property<String> relationshipDescriptor;
        private final Property<Object> relationshipMember;
        private final Property<Object> relationshipOwner;

        private RelationshipIdentityMapping(Class<?> entityClass) {
            this.entityClass = entityClass;
            this.relationshipDescriptor = PropertyQueries
                    .<String>createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(RelationshipDescriptor.class))
                    .getSingleResult();
            this.relationshipMember = PropertyQueries
                    .createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(RelationshipMember.class))
                    .getSingleResult();
            this.relationshipOwner = PropertyQueries
                    .createQuery(this.entityClass)
                    .addCriteria(new AnnotatedPropertyCriteria(OwnerReference.class))
                    .getSingleResult();
        }

        public Class<?> getEntityClass() {
            return this.entityClass;
        }

        public Property<String> getRelationshipDescriptor() {
            return relationshipDescriptor;
        }

        public Property<Object> getRelationshipMember() {
            return relationshipMember;
        }

        public Property<Object> getRelationshipOwner() {
            return relationshipOwner;
        }
    }

    public RelationshipIdentityMapping getRelationshipIdentityMapping() {
        return this.relationshipIdentityMapping;
    }

    public RelationshipMapping getRelationshipMapping() {
        return this.relationshipMapping;
    }
}
