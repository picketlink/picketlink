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

import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PermissionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.test.idm.permission.entity.TypedPermissionTypeEntity;
import org.picketlink.test.idm.util.JPAContextInitializer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.picketlink.test.idm.util.PersistenceUtil.createEntityManagerFactory;

/**
 * @author pedroigor
 */
public class JPAPermissionStoreConfigurationTester implements IdentityConfigurationTester {

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    public static final String SIMPLE_JPA_STORE_CONFIG = "SIMPLE_JPA_PERMISSION_STORE_CONFIG";

    @Override
    public DefaultPartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named(SIMPLE_JPA_STORE_CONFIG)
                .stores()
                    .jpa()
                        .mappedEntity(
                            PartitionTypeEntity.class,
                            IdentityTypeEntity.class,
                            AccountTypeEntity.class,
                            RoleTypeEntity.class,
                            GroupTypeEntity.class,
                            PermissionTypeEntity.class,
                            TypedPermissionTypeEntity.class,
                            RelationshipTypeEntity.class,
                            RelationshipIdentityTypeEntity.class
                        )
                        .addContextInitializer(new JPAContextInitializer(null) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager;
                            }
                        })
                        .supportAllFeatures()
                        .supportGlobalRelationship(Grant.class, GroupMembership.class) // to check privileges inheritance we need to define all rel types
                        .supportPermissions(true)
                        .supportCredentials(false)
                        .supportAttributes(false);

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }

        return partitionManager;
    }

    @Override
    public void beforeTest() {
        this.emf = createEntityManagerFactory("jpa-permission-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @Override
    public void afterTest() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }

    // Useful for subclasses
    public EntityManager getEntityManager() {
        return entityManager;
    }

}
