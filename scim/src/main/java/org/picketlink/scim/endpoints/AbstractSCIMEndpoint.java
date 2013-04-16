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
package org.picketlink.scim.endpoints;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
import org.picketlink.idm.model.Realm;
import org.picketlink.scim.DataProvider;
import org.picketlink.scim.providers.PicketLinkIDMDataProvider;

/**
 * Base class for SCIM Endpoints
 *
 * @author anil saldhana
 * @since Apr 16, 2013
 */
public class AbstractSCIMEndpoint {
    protected EntityManagerFactory entityManagerFactory;
    protected ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<EntityManager>();

    protected DataProvider createDefaultDataProvider() {
        PicketLinkIDMDataProvider dataProvider = new PicketLinkIDMDataProvider();

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

        IdentityManager identityManager = configuration.buildIdentityManagerFactory().createIdentityManager();

        dataProvider.setIdentityManager(identityManager);
        return dataProvider;
    }

    protected void closeEntityManager() {
        if (this.entityManagerFactory != null) {
            EntityManager entityManager = this.entityManagerThreadLocal.get();

            entityManager.getTransaction().commit();
            entityManager.close();

            this.entityManagerThreadLocal.remove();
        }
    }

    protected void initializeEntityManager() {
        if (this.entityManagerFactory != null) {
            EntityManager entityManager = this.entityManagerFactory.createEntityManager();

            entityManager.getTransaction().begin();

            this.entityManagerThreadLocal.set(entityManager);
        }
    }
}