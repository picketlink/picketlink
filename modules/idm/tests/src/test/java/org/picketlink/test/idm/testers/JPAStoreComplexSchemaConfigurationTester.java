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
import org.picketlink.idm.jpa.model.sample.complex.entity.Application;
import org.picketlink.idm.jpa.model.sample.complex.entity.ApplicationAttribute;
import org.picketlink.idm.jpa.model.sample.complex.entity.ApplicationAuthorization;
import org.picketlink.idm.jpa.model.sample.complex.entity.Company;
import org.picketlink.idm.jpa.model.sample.complex.entity.CompanyAttribute;
import org.picketlink.idm.jpa.model.sample.complex.entity.Email;
import org.picketlink.idm.jpa.model.sample.complex.entity.Employee;
import org.picketlink.idm.jpa.model.sample.complex.entity.IdentityObject;
import org.picketlink.idm.jpa.model.sample.complex.entity.OrganizationUnit;
import org.picketlink.idm.jpa.model.sample.complex.entity.OrganizationUnitAttribute;
import org.picketlink.idm.jpa.model.sample.complex.entity.RelationshipAttribute;
import org.picketlink.idm.jpa.model.sample.complex.entity.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.complex.entity.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.complex.entity.UserAccount;
import org.picketlink.idm.jpa.model.sample.complex.entity.UserAccountControl;
import org.picketlink.idm.jpa.model.sample.complex.entity.UserAttribute;
import org.picketlink.test.idm.util.JPAContextInitializer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.picketlink.test.idm.util.PersistenceUtil.createEntityManagerFactory;

/**
 * @author pedroigor
 */
public class JPAStoreComplexSchemaConfigurationTester implements IdentityConfigurationTester {

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
                        Employee.class,
                        Application.class,
                        ApplicationAttribute.class,
                        IdentityObject.class,
                        Company.class,
                        Email.class,
                        Employee.class,
                        UserAttribute.class,
                        CompanyAttribute.class,
                        UserAccount.class,
                        RelationshipAttribute.class,
                        UserAccountControl.class,
                        OrganizationUnit.class,
                        OrganizationUnitAttribute.class,
                        RelationshipTypeEntity.class,
                        RelationshipIdentityTypeEntity.class,
                        ApplicationAuthorization.class
                )
                .addContextInitializer(new JPAContextInitializer(null) {
                    @Override
                    public EntityManager getEntityManager() {
                        return entityManager;
                    }
                })
                .supportAllFeatures();

        return new DefaultPartitionManager(builder.buildAll());
    }

    @Override
    public void beforeTest() {
        this.emf = createEntityManagerFactory("complex-schema-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @Override
    public void afterTest() {
        if (this.entityManager.getTransaction().isActive()) {
            this.entityManager.getTransaction().commit();
        }

        this.entityManager.close();
        this.emf.close();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
