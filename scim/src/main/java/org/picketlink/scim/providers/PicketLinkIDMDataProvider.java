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

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.jboss.logging.Logger;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.scim.DataProvider;
import org.picketlink.scim.model.v11.SCIMGroups;
import org.picketlink.scim.model.v11.SCIMResource;
import org.picketlink.scim.model.v11.SCIMUser;
import org.picketlink.scim.model.v11.UserName;

/**
 * An IDM implementation of the {@link DataProvider}
 *
 * @author anil saldhana
 * @since Apr 10, 2013
 */
public class PicketLinkIDMDataProvider implements DataProvider {

    private static Logger log = Logger.getLogger(PicketLinkIDMDataProvider.class);
    protected EntityManagerFactory entityManagerFactory;
    protected ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();

    @Inject
    private IdentityManager identityManager;

    @Override
    public SCIMUser getUser(String id) {
        verifyIdentityManager();
        SCIMUser scimUser = new SCIMUser();

        IdentityQuery<User> query = identityManager.<User> createIdentityQuery(User.class);
        query.setParameter(IdentityType.ATTRIBUTE.byName("ID"), id);

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
        query.setParameter(IdentityType.ATTRIBUTE.byName("ID"), id);

        List<Group> result = query.getResultList();
        Group group = null;

        if (result.size() > 0) {
            group = result.get(0);

        }
        if (group != null) {
            scimGroup.setDisplayName(group.getName());
            scimGroup.setId(id);
        }
        return scimGroup;
    }

    @Override
    public SCIMResource getResource(String id) {
        SCIMResource scimResource = new SCIMResource();
        return scimResource;
    }

    @Override
    public String createUser(SCIMUser user) {
        verifyIdentityManager();

        SimpleUser simpleUser = new SimpleUser();
        simpleUser.setLoginName(user.getDisplayName());
        UserName userName = user.getName();

        if(userName != null){
            simpleUser.setFirstName(userName.getGivenName());
            simpleUser.setLastName(userName.getFamilyName());

            simpleUser.setAttribute(new Attribute<String>("FullName", userName.getFormatted()));
        }
        identityManager.add(simpleUser);

        User storedUser = identityManager.getUser(user.getDisplayName());
        String id = storedUser.getId();

        return id;
    }

    @Override
    public String createGroup(SCIMGroups group) {
        verifyIdentityManager();
        SimpleGroup simpleGroup = new SimpleGroup(group.getDisplayName());
        identityManager.add(simpleGroup);

        Group storedGroup = identityManager.getGroup(group.getDisplayName());
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
        if (this.entityManagerFactory != null) {
            EntityManager entityManager = this.entityManagerFactory.createEntityManager();

            entityManager.getTransaction().begin();

            this.entityManagerThreadLocal.set(entityManager);
        }
    }

    @Override
    public void closeConnection() {
        if (this.entityManagerFactory != null) {
            EntityManager entityManager = this.entityManagerThreadLocal.get();
            if (entityManager != null) {
                entityManager.getTransaction().commit();
                entityManager.close();
            }

            this.entityManagerThreadLocal.remove();
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

    protected void createJPADrivenIdentityManager() {
        // Use JPA
        entityManagerFactory = Persistence.createEntityManagerFactory("picketlink-scim-pu");

        IdentityConfiguration configuration = new IdentityConfiguration();

        configuration.jpaStore().addRealm(Realm.DEFAULT_REALM).setIdentityClass(IdentityObject.class)
                .setAttributeClass(IdentityObjectAttribute.class).setRelationshipClass(RelationshipObject.class)
                .setRelationshipIdentityClass(RelationshipIdentityObject.class)
                .setRelationshipAttributeClass(RelationshipObjectAttribute.class).setCredentialClass(CredentialObject.class)
                .setCredentialAttributeClass(CredentialObjectAttribute.class).setPartitionClass(PartitionObject.class)
                .supportAllFeatures().addContextInitializer(new JPAContextInitializer(entityManagerFactory) {
                    @Override
                    public EntityManager getEntityManager() {
                        return entityManagerThreadLocal.get();
                    }
                });

        identityManager = configuration.buildIdentityManagerFactory().createIdentityManager();
    }
}