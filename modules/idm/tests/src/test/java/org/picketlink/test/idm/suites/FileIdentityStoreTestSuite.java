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
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.model.Authorization;
import org.picketlink.idm.model.Realm;
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
import org.picketlink.test.idm.relationship.UserGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.UserGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.usecases.ApplicationRegistrationTestCase;
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
@SuiteClasses({ UserManagementTestCase.class, AgentManagementTestCase.class, RoleManagementTestCase.class,
        GroupManagementTestCase.class, CertificateCredentialTestCase.class, DigestCredentialTestCase.class,
        PasswordCredentialTestCase.class, GroupQueryTestCase.class, UserQueryTestCase.class, AgentQueryTestCase.class,
        RoleQueryTestCase.class, AgentGrantRelationshipTestCase.class, AgentGroupRoleRelationshipTestCase.class,
        AgentGroupsRelationshipTestCase.class, CustomRelationshipTestCase.class, GroupGrantRelationshipTestCase.class,
        GroupMembershipTestCase.class, UserGrantRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class,
        ApplicationRegistrationTestCase.class, ApplicationUserRelationshipTestCase.class, RealmManagementTestCase.class, TierManagementTestCase.class })
public class FileIdentityStoreTestSuite implements TestLifecycle {

    private static FileIdentityStoreTestSuite instance;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new FileIdentityStoreTestSuite();
        }

        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IdentityManagerFactory createIdentityManagerFactory() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        builder
            .stores()
                .file()
                    .preserveState(false)
                    .addRealm(Realm.DEFAULT_REALM, "Testing")
                    .addTier("Application")
                    .supportAllFeatures()
                    .supportRelationshipType(CustomRelationship.class, Authorization.class);
        
        return new IdentityManagerFactory(builder.build());
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onDestroy() {
    }

}
