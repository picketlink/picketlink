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
package org.picketlink.scim.providers;

import org.jboss.logging.Logger;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.AttributeTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.scim.DataProvider;
import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMResource;
import org.picketlink.scim.model.v11.SCIMUser;
import org.picketlink.scim.model.v11.UserName;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.Serializable;
import java.util.List;

/**
 * An IDM implementation of the {@link DataProvider}
 *
 * @author anil saldhana
 * @since Apr 10, 2013
 */
public class PicketLinkIDMDataProvider implements DataProvider {

    private static Logger log = Logger.getLogger(PicketLinkIDMDataProvider.class);

    //EntityManagerFactory will be null if the IdentityManager is injected in an EE environment
    protected EntityManagerFactory entityManagerFactory;

    protected ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();

    @Inject
    private IdentityManager identityManager;

    @Override
    public SCIMUser getUser(String id) {
        verifyIdentityManager();
        SCIMUser scimUser = new SCIMUser();

        IdentityQueryBuilder queryBuilder = identityManager.<User>getQueryBuilder();
        IdentityQuery<User> query = queryBuilder.createIdentityQuery(User.class);
        query.where(queryBuilder.equal(AttributedType.ID, id));

        List<User> result = query.getResultList();
        User user = null;
        if (result.size() > 0) {
            user = result.get(0);
            scimUser.setId(id);
            UserName userName = new UserName();
            userName.setGivenName(user.getFirstName());
            userName.setFamilyName(user.getLastName());
            scimUser.setName(userName);
        }
        // TODO: populate SCIM object
        return scimUser;
    }

    @Override
    public SCIMGroups getGroups(String id) {
        verifyIdentityManager();
        SCIMGroups scimGroup = new SCIMGroups();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);
        query.setParameter(AttributedType.ID, id);

        List<Group> result = query.getResultList();
        Group group = null;

        if(result.size() == 1){
            group = result.get(0);
        } else if(result.size() == 0){
            log.error("No group instances with id:" + id);
        } else {
            log.error("Multiple group instances with id:" + id);
        }
        if (group != null) {
            scimGroup.setDisplayName(group.getName());
            scimGroup.setId(id);
        }
        return scimGroup;
    }

    @Override
    public boolean deleteUser(String id) {
        verifyIdentityManager();

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);
        query.setParameter(AttributedType.ID, id);

        List<User> result = query.getResultList();
        User user = null;
        if(result.size() == 1){
            user = result.get(0);
        } else if(result.size() == 0) {
            log.error("No user instances with id:" + id);
        } else {
            log.error("Multiple user instances with id:" + id);
        }
        if(user != null){
            identityManager.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteGroup(String id) {
        verifyIdentityManager();

        IdentityQuery<Group> query = identityManager.<Group> createIdentityQuery(Group.class);
        query.setParameter(AttributedType.ID, id);

        List<Group> result = query.getResultList();
        Group group = null;
        if(result.size() == 1){
            group = result.get(0);
        } else if(result.size() == 0){
            log.error("No group instances with id:" + id);
        } else {
            log.error("Multiple group instances with id:" + id);
        }
        if (group != null) {
            identityManager.remove(group);
            return true;
        }
        return false;
    }

    @Override
    public SCIMResource getResource(String id) {
        SCIMResource scimResource = new SCIMResource();
        return scimResource;
    }

    @Override
    public String createUser(SCIMUser user) {
        verifyIdentityManager();

        User simpleUser = new User();
        simpleUser.setLoginName(user.getDisplayName());
        UserName userName = user.getName();

        if(userName != null){
            simpleUser.setFirstName(userName.getGivenName());
            simpleUser.setLastName(userName.getFamilyName());

            simpleUser.setAttribute(new Attribute<Serializable>("FullName", userName.getFormatted()));
        }
        identityManager.add(simpleUser);

        User storedUser = BasicModel.getUser(identityManager, user.getDisplayName());
        String id = storedUser.getId();

        return id;
    }

    @Override
    public String createGroup(SCIMGroups group) {
        verifyIdentityManager();
        Group simpleGroup = new Group(group.getDisplayName());
        if(group.getId() != null){
            simpleGroup.setId(group.getId());
        }

        //group.
        identityManager.add(simpleGroup);

        Group storedGroup = BasicModel.getGroup(identityManager, group.getDisplayName());
        String id = storedGroup.getId();

        return id;
    }

    public PicketLinkIDMDataProvider setIdentityManager(IdentityManager im) {
        this.identityManager = im;
        return this;
    }

    @Override
    public void initializeConnection() {
        verifyIdentityManager();

        //If we are in a non-EE environment, we have to manage the JPA stuff ourselves
        if (this.entityManagerFactory != null) {
            EntityManager entityManager = entityManagerThreadLocal.get();
            if(entityManager == null){
                entityManager = this.entityManagerFactory.createEntityManager();
                this.entityManagerThreadLocal.set(entityManager);
            }

            entityManager.getTransaction().begin();
        }
    }

    @Override
    public void closeConnection() {
        //If we are in a non-EE environment, we have to manage the JPA stuff ourselves
        if (this.entityManagerFactory != null) {
            EntityManager entityManager = this.entityManagerThreadLocal.get();
            if (entityManager != null) {
                entityManager.getTransaction().commit();
                entityManager.close();
            }

            this.entityManagerThreadLocal.remove();

            //Not originally injected. We need to create fresh
            identityManager = null;
        }
    }

    protected void verifyIdentityManager() {
        if (identityManager == null) {
            if (log.isTraceEnabled()) {
                log.trace("Identity Manager not injected. Creating JPA based Identity Manager");
            }
            createJPADrivenIdentityManager();
        }
    }

    /**
     * This is created for each connection in a non-EE environment
     */
    protected void createJPADrivenIdentityManager() {
        // Use JPA
        final EntityManager entityManager = createEntityManager();
        this.entityManagerThreadLocal.set(entityManager);
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                .jpa()
                .mappedEntity(
                        AccountTypeEntity.class,
                        RoleTypeEntity.class,
                        GroupTypeEntity.class,
                        IdentityTypeEntity.class,
                        RelationshipTypeEntity.class,
                        RelationshipIdentityTypeEntity.class,
                        PartitionTypeEntity.class,
                        PasswordCredentialTypeEntity.class,
                        AttributeTypeEntity.class)
                .supportGlobalRelationship(Relationship.class)
                .addContextInitializer(new ContextInitializer() {
                    @Override
                    public void initContextForStore(IdentityContext context, IdentityStore<?> store) {
                        context.setParameter(JPAIdentityStore.INVOCATION_CTX_ENTITY_MANAGER,entityManager);
                    }
                })
                // Specify that this identity store configuration supports all features
                .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        List<? extends Partition> partitions = partitionManager.getPartitions(Realm.class);
        boolean foundPartition = false;
        if(partitions != null){
            for(Partition partition: partitions){
                if(partition.getName().equalsIgnoreCase(Realm.DEFAULT_REALM)){
                    foundPartition = true;
                }
            }
        }

        if(!foundPartition){
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }

        identityManager = partitionManager.createIdentityManager();
    }

    private EntityManager createEntityManager(){
        // Use JPA
        entityManagerFactory = Persistence.createEntityManagerFactory("picketlink-scim-pu");
        return this.entityManagerFactory.createEntityManager();
    }
}