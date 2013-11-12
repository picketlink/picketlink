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
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.AttributeTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.DigestCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.OTPCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.X509CredentialTypeEntity;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.test.idm.basic.CustomAccountTestCase;
import org.picketlink.test.idm.basic.CustomAgentTypeEntity;
import org.picketlink.test.idm.basic.MyCustomAccountEntity;
import org.picketlink.test.idm.partition.CustomPartitionEntity;
import org.picketlink.test.idm.relationship.CustomRelationshipTypeEntity;
import org.picketlink.test.idm.util.JPAContextInitializer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.picketlink.test.idm.util.PersistenceUtil.createEntityManagerFactory;

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
                                PartitionTypeEntity.class,
                                MyCustomAccountEntity.class,
                                RoleTypeEntity.class,
                                GroupTypeEntity.class,
                                IdentityTypeEntity.class,
                                CustomRelationshipTypeEntity.class,
                                CustomAgentTypeEntity.class,
                                CustomPartitionEntity.class,
                                RelationshipTypeEntity.class,
                                RelationshipIdentityTypeEntity.class,
                                PasswordCredentialTypeEntity.class,
                                DigestCredentialTypeEntity.class,
                                X509CredentialTypeEntity.class,
                                OTPCredentialTypeEntity.class,
                                AttributeTypeEntity.class,
                                AccountTypeEntity.class
                        )
                        .supportGlobalRelationship(org.picketlink.idm.model.Relationship.class)
                        .setCredentialHandlerProperty(CredentialHandler.SUPPORTED_ACCOUNT_TYPES_PROPERTY, CustomAccountTestCase.MyCustomAccount.class)
                        .setCredentialHandlerProperty(CredentialHandler.LOGIN_NAME_PROPERTY, "userName")
                        .addContextInitializer(new JPAContextInitializer(null) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager;
                            }
                        })
                        .supportAllFeatures();

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }

        return partitionManager;
    }

    @Override
    public void beforeTest() {
        this.emf = createEntityManagerFactory("jpa-identity-store-tests-pu");
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
    protected EntityManager getEntityManager() {
        return entityManager;
    }

}
