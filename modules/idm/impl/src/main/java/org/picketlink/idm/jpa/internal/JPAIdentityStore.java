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

package org.picketlink.idm.jpa.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.picketlink.common.properties.Property;
import org.picketlink.common.util.StringUtil;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.AbstractModel;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.ModelDefinition;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration.PropertyMapping;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.TOTPCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.internal.util.RelationshipMetadata;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * Implementation of IdentityStore that stores its state in a relational database. This is a lightweight object that is
 * generally created once per request, and is provided references to a (heavyweight) configuration and invocation context.
 *
 * @author Shane Bryzak
 * @author Pedro Silva
 */
@CredentialHandlers({PasswordCredentialHandler.class, X509CertificateCredentialHandler.class, DigestCredentialHandler.class, TOTPCredentialHandler.class})
public class JPAIdentityStore implements CredentialStore<JPAIdentityStoreConfiguration>, PartitionStore<JPAIdentityStoreConfiguration> {

    // Invocation context parameters
    public static final String INVOCATION_CTX_ENTITY_MANAGER = "CTX_ENTITY_MANAGER";

    // Event context parameters
    public static final String EVENT_CONTEXT_IDENTITY = "IDENTITY_ENTITY";

    /**
     * The configuration for this instance
     */
    private JPAIdentityStoreConfiguration config;

    private RelationshipMetadata relationshipMetadata = new RelationshipMetadata();

    @Override
    public void setup(JPAIdentityStoreConfiguration config) {
        this.config = config;
    }

    @Override
    public JPAIdentityStoreConfiguration getConfig() {
        return config;
    }

    @Override
    public void add(IdentityContext context, AttributedType value) {
        if (IdentityType.class.isInstance(value)) {
            EntityGraph graph = EntityGraph.create(value, config.getIdentityModel());
            graph.setProperty(config.getIdentityModel().getIdentityClassProperty(), value.getClass().getName());
            graph.persist(getEntityManager(context));
        } else if (Relationship.class.isInstance(value)) {
            Relationship relationship = (Relationship) value;
            EntityGraph graph = EntityGraph.create(relationship, config.getRelationshipModel());
            graph.setProperty(config.getRelationshipModel().getRelationshipClassProperty(), relationship.getClass().getName());

            // For each of the identities participating in the relationship, create a new node in the graph
            Class<?> relationshipIdentityClass = config.getRelationshipModel().getRelationshipMember().getDeclaringClass();

            Set<Property<? extends IdentityType>> identityProperties = relationshipMetadata.getRelationshipIdentityProperties(
                    relationship.getClass());

            for (Property<? extends IdentityType> property : identityProperties) {
                Object entity = graph.createEntity(relationshipIdentityClass);
                graph.createNode(entity, false);

                // If the relationship member property is a String, set the identifier as the value 
                if (String.class.equals(config.getRelationshipModel().getRelationshipMember().getJavaClass())) {
                    IdentityType relationshipIdentity = property.getValue(relationship);
                    // We use the convention "Identity ID:Partition ID" to store identity references
                    // TODO maybe replace this with an IdentityReference instead?
                    graph.setProperty(config.getRelationshipModel().getRelationshipMember(),
                            String.format("%s:%s", relationshipIdentity.getId(), relationshipIdentity.getPartition().getId()));
                } else {
                    // Otherwise we set the value to the entity with the specified identifier
                    AttributedType member = (AttributedType) config.getRelationshipModel().getRelationshipMember().getValue(relationship);
                    String identifier = member.getId();

                    Object identityEntity = lookupEntityByParameter(context, config.getIdentityModel(), IdentityType.class, "id", identifier);
                    graph.setProperty(config.getRelationshipModel().getRelationshipMember(), identityEntity);
                }

                graph.setProperty(config.getRelationshipModel().getRelationshipDescriptor(), property.getName());

                // TODO set the owner property
            }

            graph.persist(getEntityManager(context));
        }
    }

    @Override
    public void update(IdentityContext context, AttributedType value) {
        if (IdentityType.class.isInstance(value)) {
            EntityGraph graph = EntityGraph.load(config.getIdentityModel(), value.getClass(), value.getId());
            graph.update(value);
            graph.persist(getEntityManager(context));
        } else if (Relationship.class.isInstance(value)) {
            EntityGraph graph = EntityGraph.load(config.getRelationshipModel(), value.getClass(), value.getId());
            graph.update(value);
            graph.persist(getEntityManager(context));
        }
    }

    @Override
    public void remove(IdentityContext context, AttributedType value) {
        if (IdentityType.class.isInstance(value)) {
            EntityGraph graph = EntityGraph.load(config.getIdentityModel(), value.getClass(), value.getId());
            graph.delete(getEntityManager(context));
        } else if (Relationship.class.isInstance(value)) {
            EntityGraph graph = EntityGraph.load(config.getRelationshipModel(), value.getClass(), value.getId());
            graph.delete(getEntityManager(context));
        }
    }

    private Object lookupEntityByParameter(IdentityContext context, AbstractModel model, Class<?> cls, String propertyName, 
            Object value) {
        if (StringUtil.isNullOrEmpty(propertyName)) {
            throw new IllegalArgumentException("propertyName parameter must contain a value");
        }

        Set<ModelDefinition> definitions = model.getDefinitions(cls);
        for (ModelDefinition definition : definitions) {
            for (Property property : definition.getProperties().keySet()) {
                if (propertyName.equals(property.getName())) {
                    PropertyMapping mapping = definition.getProperties().get(property);
                    EntityManager em = getEntityManager(context);

                    Property<Object> entityProperty = mapping.getEntityProperty();

                    CriteriaBuilder builder = em.getCriteriaBuilder();
                    CriteriaQuery<?> query = builder.createQuery(entityProperty.getDeclaringClass());
                    Root<?> root = query.from(entityProperty.getDeclaringClass());

                    List<Predicate> predicates = new ArrayList<Predicate>();
                    predicates.add(builder.equal(root.get(entityProperty.getName()), value));
                    return em.createQuery(query).getSingleResult();
                }
            }
        }

        return null;
    }

    protected EntityManager getEntityManager(IdentityContext context) {
        if (!context.isParameterSet(INVOCATION_CTX_ENTITY_MANAGER)) {
            throw IDMMessages.MESSAGES.jpaStoreCouldNotGetEntityManagerFromStoreContext();
        }

        return (EntityManager) context.getParameter(INVOCATION_CTX_ENTITY_MANAGER);
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        EntityGraph graph = EntityGraph.create(storage, config.getCredentialModel());
        graph.persist(getEntityManager(context));
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCurrentCredential
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCredentials
    }

    @Override
    public <I extends IdentityType> I getIdentity(Class<I> identityType, String id) {
        return null;  //TODO: Implement getIdentity
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        return null;  //TODO: Implement fetchQueryResults
    }

    @Override
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return null;  //TODO: Implement fetchQueryResults
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public void validateCredentials(IdentityContext context, Credentials credentials) {
     // TODO move logic in FileIdentityStore to abstract base IdentityStore class
    }

    @Override
    public void updateCredential(IdentityContext context, Account account, Object credential, Date effectiveDate, Date expiryDate) {
        // TODO move logic in FileIdentityStore to abstract base IdentityStore class
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void add(IdentityContext context, Partition partition, String configurationName) {
        EntityGraph graph = EntityGraph.create(partition, config.getPartitionModel());
        graph.setProperty(config.getPartitionModel().getPartitionClassProperty(), partition.getClass().getName());
        graph.persist(getEntityManager(context));

    }

    @Override
    public void update(IdentityContext context, Partition partition) {
        EntityGraph graph = EntityGraph.load(config.getPartitionModel(), partition.getClass(), partition.getId());
        graph.update(partition);
        graph.persist(getEntityManager(context));
    }

    @Override
    public void remove(IdentityContext context, Partition partition) {
        EntityGraph graph = EntityGraph.load(config.getPartitionModel(), partition.getClass(), partition.getId());
        graph.delete(getEntityManager(context));
    }
}