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

import java.io.Serializable;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.jboss.logging.Logger;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
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
        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("ID"), id);

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
        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("ID"), id);

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

        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportAllFeatures();

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        // FIXME: IdentityManager is not threadsafe
        identityManager = partitionManager.createIdentityManager();
    }
}