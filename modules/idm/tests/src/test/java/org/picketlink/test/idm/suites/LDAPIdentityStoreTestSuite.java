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
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.IdentityManagerRunner;
import org.picketlink.test.idm.TestLifecycle;
import org.picketlink.test.idm.basic.UserManagementTestCase;

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
        UserManagementTestCase.class
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
                    .userDNSuffix(USER_DN_SUFFIX)
                    .roleDNSuffix(ROLES_DN_SUFFIX)
                    .agentDNSuffix(AGENT_DN_SUFFIX)
                    .groupDNSuffix(GROUP_DN_SUFFIX)
                    .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
                    .supportType(IdentityType.class)
                    .mapping(User.class)
                        .baseDN(USER_DN_SUFFIX)
                        .objectClasses("inetOrgPerson", "organizationalPerson", "person", "top", "extensibleObject")
                        .attribute("loginName", "uid", true)
                        .attribute("firstName", "cn")
                        .attribute("lastName", "sn")
                        .attribute("email", "mail");

        return new DefaultPartitionManager(builder.build());
    }

}
