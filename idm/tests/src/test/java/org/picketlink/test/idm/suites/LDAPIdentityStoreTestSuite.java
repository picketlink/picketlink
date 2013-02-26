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
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.test.idm.IdentityManagerRunner;
import org.picketlink.test.idm.TestLifecycle;
import org.picketlink.test.idm.basic.AgentManagementTestCase;
import org.picketlink.test.idm.basic.GroupManagementTestCase;
import org.picketlink.test.idm.basic.RoleManagementTestCase;
import org.picketlink.test.idm.basic.UserManagementTestCase;
import org.picketlink.test.idm.credential.PasswordCredentialTestCase;
import org.picketlink.test.idm.query.AgentQueryTestCase;
import org.picketlink.test.idm.query.GroupQueryTestCase;
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
 * Test suite for the {@link IdentityManager} using a {@link LDAPIdentityStore}.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ UserManagementTestCase.class, PasswordCredentialTestCase.class, RoleManagementTestCase.class, GroupManagementTestCase.class,
        AgentManagementTestCase.class, AgentQueryTestCase.class, UserQueryTestCase.class, RoleQueryTestCase.class,
        GroupQueryTestCase.class, AgentGroupRoleRelationshipTestCase.class, AgentGroupsRelationshipTestCase.class,
        UserGrantRelationshipTestCase.class, AgentGrantRelationshipTestCase.class, GroupGrantRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class, GroupMembershipTestCase.class
         })
public class LDAPIdentityStoreTestSuite extends AbstractLDAPTest implements TestLifecycle {

    private static final String BASE_DN = "dc=jboss,dc=org";
    private static final String LDAP_URL = "ldap://localhost:10389";
    private static final String ROLES_DN_SUFFIX = "ou=Roles,dc=jboss,dc=org";
    private static final String GROUP_DN_SUFFIX = "ou=Groups,dc=jboss,dc=org";
    private static final String USER_DN_SUFFIX = "ou=People,dc=jboss,dc=org";
    private static final String AGENT_DN_SUFFIX = "ou=Agent,dc=jboss,dc=org";
    
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
    public IdentityManager createIdentityManager() {
        IdentityConfiguration config = new IdentityConfiguration();

        config.addStoreConfiguration(getConfiguration());

        IdentityManager identityManager = new DefaultIdentityManager();

        identityManager.bootstrap(config, new DefaultIdentityStoreInvocationContextFactory(null));

        return identityManager;
    }

    @Override
    public void onDestroy() {

    }

    public static LDAPIdentityStoreConfiguration getConfiguration() {

        LDAPIdentityStoreConfiguration config = new LDAPIdentityStoreConfiguration();

        config.setBaseDN(BASE_DN).setBindDN("uid=admin,ou=system").setBindCredential("secret").setLdapURL(LDAP_URL)
                .setUserDNSuffix(USER_DN_SUFFIX).setRoleDNSuffix(ROLES_DN_SUFFIX).setAgentDNSuffix(AGENT_DN_SUFFIX)
                .setGroupDNSuffix(GROUP_DN_SUFFIX);

        config.addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org");

        FeatureSet.addFeatureSupport(config.getFeatureSet(), FeatureGroup.agent, FeatureGroup.user, 
                FeatureGroup.group, FeatureGroup.role, FeatureGroup.relationship, FeatureGroup.credential);
        FeatureSet.addRelationshipSupport(config.getFeatureSet(), Grant.class, GroupMembership.class);
        config.getFeatureSet().setSupportsCustomRelationships(false);
        config.getFeatureSet().setSupportsMultiRealm(false);

        return config;
    }

    @Override
    public void importLDIF(String fileName) throws Exception {
        super.importLDIF(fileName);
    }
}
