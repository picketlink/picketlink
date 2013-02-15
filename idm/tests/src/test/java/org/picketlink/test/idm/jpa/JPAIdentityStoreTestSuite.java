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

package org.picketlink.test.idm.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.file.internal.FileIdentityStoreConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.test.idm.IdentityManagerRunner;
import org.picketlink.test.idm.TestLifecycle;
import org.picketlink.test.idm.basic.AgentManagementTestCase;
import org.picketlink.test.idm.basic.GroupManagementTestCase;
import org.picketlink.test.idm.basic.RoleManagementTestCase;
import org.picketlink.test.idm.basic.UserManagementTestCase;
import org.picketlink.test.idm.credential.CertificateCredentialTestCase;
import org.picketlink.test.idm.credential.DigestCredentialTestCase;
import org.picketlink.test.idm.credential.PasswordCredentialTestCase;
import org.picketlink.test.idm.partition.RealmManagementTestCase;
import org.picketlink.test.idm.partition.TierManagementTestCase;
import org.picketlink.test.idm.query.AgentQueryTestCase;
import org.picketlink.test.idm.query.GroupQueryTestCase;
import org.picketlink.test.idm.query.RoleQueryTestCase;
import org.picketlink.test.idm.query.UserQueryTestCase;
import org.picketlink.test.idm.relationship.AgentGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupsRelationshipTestCase;
import org.picketlink.test.idm.relationship.CustomRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupMembershipTestCase;
import org.picketlink.test.idm.relationship.UserGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.UserGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.usecases.ApplicationUserRelationshipTestCase;

/**
 * <p>
 * Test suite for the {@link IdentityManager} using a {@link JPAIdentityStore}. For each test is created a fresh
 * {@link IdentityManager} instance. Data is not preserved between tests.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ CustomRelationshipTestCase.class, RealmManagementTestCase.class, TierManagementTestCase.class, GroupMembershipTestCase.class,
    ApplicationUserRelationshipTestCase.class, UserManagementTestCase.class, AgentManagementTestCase.class,
    RoleManagementTestCase.class, GroupManagementTestCase.class, AgentGroupsRelationshipTestCase.class,
    UserGrantRelationshipTestCase.class, AgentGrantRelationshipTestCase.class, GroupGrantRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class,
    AgentGroupRoleRelationshipTestCase.class, RoleQueryTestCase.class, GroupQueryTestCase.class, UserQueryTestCase.class,
    AgentQueryTestCase.class, PasswordCredentialTestCase.class, CertificateCredentialTestCase.class, DigestCredentialTestCase.class })
public class JPAIdentityStoreTestSuite implements TestLifecycle {

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    public static TestLifecycle init() throws Exception {
        return new JPAIdentityStoreTestSuite();
    }

    @Override
    public void onInit() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @Override
    public IdentityManager createIdentityManager() {
        IdentityConfiguration config = new IdentityConfiguration();

        config.addStoreConfiguration(getDefaultConfiguration());
        config.addStoreConfiguration(getTestingRealmConfiguration());
        
        IdentityManager identityManager = new DefaultIdentityManager();
        DefaultIdentityStoreInvocationContextFactory icf = new DefaultIdentityStoreInvocationContextFactory(emf);
        icf.setEntityManager(entityManager);
        identityManager.bootstrap(config, icf);

        return identityManager;
    }

    /**
     * <p>Returns a specific {@link FileIdentityStoreConfiguration} for the Realm.DEFAULT_REALM.</p>
     * 
     * @return
     */
    private IdentityStoreConfiguration getDefaultConfiguration() {
        JPAIdentityStoreConfiguration configuration = new JPAIdentityStoreConfiguration();

        configureJPAConfiguration(configuration);

        return configuration;
    }
    
    /**
     * <p>Returns a specific {@link FileIdentityStoreConfiguration} for the Testing Realm.</p>
     * 
     * @return
     */
    private IdentityStoreConfiguration getTestingRealmConfiguration() {
        JPAIdentityStoreConfiguration configuration = new JPAIdentityStoreConfiguration();
        
        configuration.setRealm("Testing");
        
        configureJPAConfiguration(configuration);
        
        return configuration;
    }

    private void configureJPAConfiguration(JPAIdentityStoreConfiguration configuration) {
        configuration.setIdentityClass(IdentityObject.class);
        configuration.setAttributeClass(IdentityObjectAttribute.class);
        configuration.setRelationshipClass(RelationshipObject.class);
        configuration.setRelationshipIdentityClass(RelationshipIdentityObject.class);
        configuration.setRelationshipAttributeClass(RelationshipObjectAttribute.class);
        configuration.setCredentialClass(CredentialObject.class);
        configuration.setCredentialAttributeClass(CredentialObjectAttribute.class);
        configuration.setPartitionClass(PartitionObject.class);
    }

    @Override
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }

}
