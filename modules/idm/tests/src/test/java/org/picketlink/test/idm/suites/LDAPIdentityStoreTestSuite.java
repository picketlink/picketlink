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
import org.junit.runners.Suite;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.ldap.internal.LDAPConstants;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.IdentityManagerRunner;
import org.picketlink.test.idm.TestLifecycle;
import org.picketlink.test.idm.basic.AgentManagementTestCase;
import org.picketlink.test.idm.basic.CustomIdentityTypeTestCase;
import org.picketlink.test.idm.basic.GroupManagementTestCase;
import org.picketlink.test.idm.basic.RoleManagementTestCase;
import org.picketlink.test.idm.basic.UserManagementTestCase;
import org.picketlink.test.idm.relationship.AgentGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupMembershipTestCase;
import org.picketlink.test.idm.relationship.UserGrantRelationshipTestCase;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.EMAIL;
import static org.picketlink.idm.ldap.internal.LDAPConstants.GROUP_OF_NAMES;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;
import static org.picketlink.test.idm.basic.CustomIdentityTypeTestCase.MyCustomIdentityType;

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
@Suite.SuiteClasses({
        AgentManagementTestCase.class, UserManagementTestCase.class, RoleManagementTestCase.class,
        CustomIdentityTypeTestCase.class, GroupManagementTestCase.class,
        UserGrantRelationshipTestCase.class, AgentGrantRelationshipTestCase.class, GroupGrantRelationshipTestCase.class,
        GroupMembershipTestCase.class
})
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
                    .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
                    .supportType(IdentityType.class)
                    .supportGlobalRelationship(Grant.class, GroupMembership.class)
                    .mapping(Agent.class)
                        .baseDN(AGENT_DN_SUFFIX)
                        .objectClasses("account")
                        .attribute("loginName", UID, true)
                    .mapping(User.class)
                        .baseDN(USER_DN_SUFFIX)
                        .objectClasses("inetOrgPerson", "organizationalPerson")
                        .attribute("loginName", UID, true)
                        .attribute("firstName", CN)
                        .attribute("lastName", SN)
                        .attribute("email", EMAIL)
                    .mapping(Role.class)
                        .baseDN(ROLES_DN_SUFFIX)
                        .objectClasses(GROUP_OF_NAMES)
                        .attribute("name", CN, true)
                    .mapping(Group.class)
                        .baseDN(GROUP_DN_SUFFIX)
                        .objectClasses(GROUP_OF_NAMES)
                        .attribute("name", CN, true)
                        .parentMembershipAttributeName("member")
                    .mapping(MyCustomIdentityType.class)
                        .baseDN("ou=CustomTypes,dc=jboss,dc=org")
                        .objectClasses("device")
                        .attribute("someIdentifier", CN, true)
                        .attribute("someAttribute", "description")
                    .mappingRelationship(Grant.class)
                        .forMapping(Role.class)
                        .attribute("assignee", "member")
                    .mappingRelationship(GroupMembership.class)
                        .forMapping(Group.class)
                        .attribute("member", "member")

        ;

        return new DefaultPartitionManager(builder.build());
    }

}
