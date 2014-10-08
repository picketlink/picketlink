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
import org.picketlink.test.idm.identitymodel.complex.model.CustomerUser;
import org.picketlink.test.idm.identitymodel.complex.model.EmployeeUser;
import org.picketlink.test.idm.identitymodel.complex.model.entity.ApplicationAuthorizationEntity;
import org.picketlink.test.idm.identitymodel.complex.model.entity.ApplicationEntity;
import org.picketlink.test.idm.identitymodel.complex.model.entity.ApplicationAttribute;
import org.picketlink.test.idm.identitymodel.complex.model.entity.CompanyEntity;
import org.picketlink.test.idm.identitymodel.complex.model.entity.CompanyAttribute;
import org.picketlink.test.idm.identitymodel.complex.model.entity.Email;
import org.picketlink.test.idm.identitymodel.complex.model.entity.Employee;
import org.picketlink.test.idm.identitymodel.complex.model.entity.IdentityObject;
import org.picketlink.test.idm.identitymodel.complex.model.entity.OrganizationUnitEntity;
import org.picketlink.test.idm.identitymodel.complex.model.entity.OrganizationUnitAttribute;
import org.picketlink.test.idm.identitymodel.complex.model.entity.RelationshipAttribute;
import org.picketlink.test.idm.identitymodel.complex.model.entity.RelationshipIdentityTypeEntity;
import org.picketlink.test.idm.identitymodel.complex.model.entity.RelationshipTypeEntity;
import org.picketlink.test.idm.identitymodel.complex.model.entity.UserAccount;
import org.picketlink.test.idm.identitymodel.complex.model.entity.UserAccountControl;
import org.picketlink.test.idm.identitymodel.complex.model.entity.UserAttribute;
import org.picketlink.test.idm.util.JPAContextInitializer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

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
                        ApplicationEntity.class,
                        ApplicationAttribute.class,
                        IdentityObject.class,
                        CompanyEntity.class,
                        Email.class,
                        Employee.class,
                        UserAttribute.class,
                        CompanyAttribute.class,
                        UserAccount.class,
                        RelationshipAttribute.class,
                        UserAccountControl.class,
                        OrganizationUnitEntity.class,
                        OrganizationUnitAttribute.class,
                        RelationshipTypeEntity.class,
                        RelationshipIdentityTypeEntity.class,
                        ApplicationAuthorizationEntity.class
                )
                .addContextInitializer(new JPAContextInitializer(null) {
                    @Override
                    public EntityManager getEntityManager() {
                        return entityManager;
                    }
                })
                .supportType(
                    org.picketlink.test.idm.identitymodel.complex.model.Application.class,
                    org.picketlink.test.idm.identitymodel.complex.model.Company.class,
                    CustomerUser.class,
                    EmployeeUser.class,
                    org.picketlink.test.idm.identitymodel.complex.model.OrganizationUnit.class)
                .supportGlobalRelationship(
                    org.picketlink.test.idm.identitymodel.complex.model.ApplicationAuthorization.class
                )
                .supportAttributes(true);

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
