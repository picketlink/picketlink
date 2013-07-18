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
package org.picketlink.test.idm.testers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.test.idm.model.complex.entity.AccountLogin;
import org.picketlink.test.idm.model.complex.entity.AuthorizationRelationshipEntity;
import org.picketlink.test.idm.model.complex.entity.CredentialAttribute;
import org.picketlink.test.idm.model.complex.entity.CustomIdentityTypeObject;
import org.picketlink.test.idm.model.complex.entity.GroupAttribute;
import org.picketlink.test.idm.model.complex.entity.IdentityAttribute;
import org.picketlink.test.idm.model.complex.entity.IdentityObject;
import org.picketlink.test.idm.model.complex.entity.IdentityPartition;
import org.picketlink.test.idm.model.complex.entity.Relationship;
import org.picketlink.test.idm.model.complex.entity.RelationshipIdentityObject;
import org.picketlink.test.idm.model.complex.entity.RoleAttribute;
import org.picketlink.test.idm.model.complex.entity.UserAddress;
import org.picketlink.test.idm.model.complex.entity.UserAttribute;

/**
 * @author pedroigor
 */
public class JPAStoreConfigurationTester implements IdentityConfigurationTester {

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    public static final String SIMPLE_JPA_STORE_CONFIG = "SIMPLE_JPA_STORE_CONFIG";

    @Override
    public DefaultPartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named(SIMPLE_JPA_STORE_CONFIG)
                .stores()
                    .jpa()
                        .mappedEntity(
                                IdentityObject.class,
                                UserAttribute.class,
                                AccountLogin.class,
                                RoleAttribute.class,
                                GroupAttribute.class,
                                IdentityAttribute.class,
                                CredentialAttribute.class,
                                IdentityPartition.class,
                                UserAddress.class,
                                Relationship.class,
                                AuthorizationRelationshipEntity.class,
                                RelationshipIdentityObject.class,
                                CustomIdentityTypeObject.class)
                        .supportGlobalRelationship(org.picketlink.idm.model.Relationship.class)
                        .addContextInitializer(new JPAContextInitializer(null) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager;
                            }
                        })
                        .supportAllFeatures();

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        return partitionManager;
    }

    @Override
    public void beforeTest() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @Override
    public void afterTest() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }

    @Override
    public void commit() {
        this.entityManager.getTransaction().commit();
    }
}
