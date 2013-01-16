/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.test.idm.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.test.idm.AgentGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.AgentGroupsRelationshipTestCase;
import org.picketlink.test.idm.AgentManagementTestCase;
import org.picketlink.test.idm.AgentQueryTestCase;
import org.picketlink.test.idm.AgentRolesRelationshipTestCase;
import org.picketlink.test.idm.ApplicationUserRelationshipTestCase;
import org.picketlink.test.idm.CertificateCredentialTestCase;
import org.picketlink.test.idm.GroupManagementTestCase;
import org.picketlink.test.idm.GroupQueryTestCase;
import org.picketlink.test.idm.PasswordCredentialTestCase;
import org.picketlink.test.idm.RoleManagementTestCase;
import org.picketlink.test.idm.RoleQueryTestCase;
import org.picketlink.test.idm.UserGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.UserManagementTestCase;
import org.picketlink.test.idm.UserQueryTestCase;
import org.picketlink.test.idm.UserRolesRelationshipTestCase;
import org.picketlink.test.idm.runners.IdentityManagerRunner;
import org.picketlink.test.idm.runners.TestLifecycle;

/**
 * <p>
 * Test suite for the {@link IdentityManager} using a {@link JPAIdentityStore}.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ UserManagementTestCase.class, RoleManagementTestCase.class, GroupManagementTestCase.class,
        UserRolesRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class,
        RoleQueryTestCase.class, GroupQueryTestCase.class, UserQueryTestCase.class, AgentManagementTestCase.class,
        AgentQueryTestCase.class, AgentGroupsRelationshipTestCase.class, AgentRolesRelationshipTestCase.class,
        AgentGroupRoleRelationshipTestCase.class, PasswordCredentialTestCase.class, CertificateCredentialTestCase.class, ApplicationUserRelationshipTestCase.class })
public class JPAIdentityStoreTestSuite implements TestLifecycle {

    protected static EntityManagerFactory emf;

    public static TestLifecycle init() throws Exception {
        return new JPAIdentityStoreTestSuite();
    }

    /**
     * <p>
     * Creates a shared {@link EntityManagerFactory} and database instances
     * </p>
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void onBeforeTests() throws Exception {
        emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
    }

    /**
     * <p>
     * Closes the shared {@link EntityManagerFactory} instance.
     * </p>
     * 
     * @throws Exception
     */
    @AfterClass
    public static void onAfterTests() throws Exception {
        emf.close();
    }

    private EntityManager entityManager;

    @Override
    public void onInit() {
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @Override
    public IdentityManager createIdentityManager() {
        IdentityConfiguration config = new IdentityConfiguration();

        config.addStoreConfiguration(getConfiguration());

        IdentityManager identityManager = new DefaultIdentityManager();
        DefaultIdentityStoreInvocationContextFactory icf = new DefaultIdentityStoreInvocationContextFactory(emf);
        icf.setEntityManager(entityManager);
        identityManager.bootstrap(config, icf);

        return identityManager;
    }

    private IdentityStoreConfiguration getConfiguration() {
        JPAIdentityStoreConfiguration configuration = new JPAIdentityStoreConfiguration();

        configuration.setIdentityClass(IdentityObject.class);
        configuration.setAttributeClass(IdentityObjectAttribute.class);
//        configuration.setRelationshipClass(RelationshipObject.class);
        configuration.setCredentialClass(CredentialObject.class);
        configuration.setCredentialAttributeClass(CredentialObjectAttribute.class);
        configuration.setPartitionClass(PartitionObject.class);

        return configuration;
    }

    @Override
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
    }

}
