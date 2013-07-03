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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.test.idm.IdentityManagerRunner;
import org.picketlink.test.idm.TestLifecycle;
import org.picketlink.test.idm.basic.AgentManagementTestCase;
import org.picketlink.test.idm.basic.GroupManagementTestCase;
import org.picketlink.test.idm.basic.RoleManagementTestCase;
import org.picketlink.test.idm.basic.UserManagementTestCase;
import org.picketlink.test.idm.credential.PasswordCredentialTestCase;
import org.picketlink.test.idm.query.AgentQueryTestCase;
import org.picketlink.test.idm.query.GroupQueryTestCase;
import org.picketlink.test.idm.query.RelationshipQueryTestCase;
import org.picketlink.test.idm.query.RoleQueryTestCase;
import org.picketlink.test.idm.query.UserQueryTestCase;
import org.picketlink.test.idm.relationship.AgentGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupsRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupMembershipTestCase;
import org.picketlink.test.idm.relationship.UserGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.UserGroupRoleRelationshipTestCase;

/**
 * <p>
 * Test suite for the {@link IdentityManager} using a {@link LDAPIdentityStore}. This suites uses a embedded Apache DS server
 * during the tests. The same server instance is used by all test cases.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ RelationshipQueryTestCase.class, UserManagementTestCase.class, PasswordCredentialTestCase.class,
    RoleManagementTestCase.class, GroupManagementTestCase.class, AgentManagementTestCase.class, AgentQueryTestCase.class,
    UserQueryTestCase.class, RoleQueryTestCase.class, GroupQueryTestCase.class, AgentGroupRoleRelationshipTestCase.class,
    AgentGroupsRelationshipTestCase.class, UserGrantRelationshipTestCase.class, AgentGrantRelationshipTestCase.class,
    GroupGrantRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class, GroupMembershipTestCase.class })
public class LDAPIdentityStoreTestSuite extends LDAPAbstractSuite implements TestLifecycle {

    private static LDAPIdentityStoreTestSuite instance;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new LDAPIdentityStoreTestSuite();
        }

        return instance;
    }

    @BeforeClass
    public static void onBeforeClass() {
        try {
            init();
            instance.setup();
            instance.importLDIF("ldap/users.ldif");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void onDestroyClass() {
        try {
            instance.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInit() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public PartitionManager createPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
            .stores()
                .ldap()
                    .baseDN(BASE_DN)
                    .bindDN("uid=admin,ou=system")
                    .bindCredential("secret")
                    .url(LDAP_URL)
                    .userDNSuffix(USER_DN_SUFFIX)
                    .roleDNSuffix(ROLES_DN_SUFFIX)
                    .agentDNSuffix(AGENT_DN_SUFFIX)
                    .groupDNSuffix(GROUP_DN_SUFFIX)
                    .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
                    .supportAllFeatures();

        return null;
//        return new IdentityManagerFactory(builder.build());
    }

}
