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

package org.picketlink.test.idm.suites;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.file.internal.FileDataSource;
import org.picketlink.idm.file.internal.FileIdentityStoreConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.model.Authorization;
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
import org.picketlink.test.idm.relationship.CustomRelationship;
import org.picketlink.test.idm.relationship.CustomRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupMembershipTestCase;
import org.picketlink.test.idm.relationship.RelationshipQueryTestCase;
import org.picketlink.test.idm.relationship.UserGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.UserGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.usecases.ApplicationUserRelationshipTestCase;

/**
 * <p>
 * Test suite for the {@link IdentityManager} using a {@link FileBasedIdentityStore}. For each test is created a fresh
 * {@link IdentityManager} instance. Data is not preserved between tests.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ RelationshipQueryTestCase.class, CustomRelationshipTestCase.class, RealmManagementTestCase.class, TierManagementTestCase.class, GroupMembershipTestCase.class,
        ApplicationUserRelationshipTestCase.class, UserManagementTestCase.class, AgentManagementTestCase.class,
        RoleManagementTestCase.class, GroupManagementTestCase.class, AgentGroupsRelationshipTestCase.class,
        UserGrantRelationshipTestCase.class, AgentGrantRelationshipTestCase.class, GroupGrantRelationshipTestCase.class,UserGroupRoleRelationshipTestCase.class,
        AgentGroupRoleRelationshipTestCase.class, RoleQueryTestCase.class, GroupQueryTestCase.class, UserQueryTestCase.class,
        AgentQueryTestCase.class, PasswordCredentialTestCase.class, CertificateCredentialTestCase.class,
        DigestCredentialTestCase.class })
public class FileIdentityStoreTestSuite implements TestLifecycle {

    private static FileIdentityStoreTestSuite instance;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new FileIdentityStoreTestSuite();
        }

        return instance;
    }

    @Override
    public IdentityManager createIdentityManager() {
        IdentityConfiguration config = new IdentityConfiguration();

        FileDataSource dataSource = new FileDataSource();

        dataSource.setAlwaysCreateFiles(true);
        
        addDefaultConfiguration(config, dataSource);
        addTestingRealmConfiguration(config, dataSource);

        IdentityManager identityManager = new DefaultIdentityManager();

        identityManager.bootstrap(config, new DefaultIdentityStoreInvocationContextFactory(null));

        return identityManager;
    }

    /**
     * <p>Configure a specific {@link FileIdentityStoreConfiguration} for the Testing Realm.</p>
     * 
     * @param config
     * @param dataSource
     */
    private void addTestingRealmConfiguration(IdentityConfiguration config, FileDataSource dataSource) {
        FileIdentityStoreConfiguration fileConfig = new FileIdentityStoreConfiguration();

        fileConfig.addRealm("Testing");

        fileConfig.setDataSource(dataSource);

        FeatureSet.addFeatureSupport(fileConfig.getFeatureSet());
        FeatureSet.addRelationshipSupport(fileConfig.getFeatureSet());
        fileConfig.getFeatureSet().setSupportsCustomRelationships(true);
        fileConfig.getFeatureSet().setSupportsMultiRealm(true);

        config.addStoreConfiguration(fileConfig);
    }

    /**
     * <p>
     * Configure a specific {@link FileIdentityStoreConfiguration} for the Realm.DEFAULT_REALM.
     * </p>
     * 
     * @param config
     * @param dataSource
     */
    private void addDefaultConfiguration(IdentityConfiguration config, FileDataSource dataSource) {
        FileIdentityStoreConfiguration configuration = new FileIdentityStoreConfiguration();

        configuration.setDataSource(dataSource);

        FeatureSet.addFeatureSupport(configuration.getFeatureSet());
        FeatureSet.addRelationshipSupport(configuration.getFeatureSet());
        FeatureSet.addRelationshipSupport(configuration.getFeatureSet(), CustomRelationship.class);
        FeatureSet.addRelationshipSupport(configuration.getFeatureSet(), Authorization.class);
        configuration.getFeatureSet().setSupportsCustomRelationships(true);
        configuration.getFeatureSet().setSupportsMultiRealm(true);

        config.addStoreConfiguration(configuration);
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onDestroy() {

    }

}
