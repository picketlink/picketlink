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

package org.picketlink.test.idm.suites;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.test.idm.GroupManagementTestCase;
import org.picketlink.test.idm.GroupQueryTestCase;
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
 * Test suite for the {@link IdentityManager} using a {@link LDAPIdentityStore}.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ UserManagementTestCase.class, RoleManagementTestCase.class, GroupManagementTestCase.class,
    UserRolesRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class,
    RoleQueryTestCase.class, GroupQueryTestCase.class, UserQueryTestCase.class})
public class LDAPIdentityStoreTestSuite extends AbstractLDAPTest implements TestLifecycle {

    private static LDAPIdentityStoreTestSuite instance;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new LDAPIdentityStoreTestSuite();
        }

        return instance;
    }

    private static final String LDAP_URL = "ldap://localhost:10389";
    private static final String ROLES_DN_SUFFIX = "ou=Roles,dc=jboss,dc=org";
    private static final String GROUP_DN_SUFFIX = "ou=Groups,dc=jboss,dc=org";
    private static final String USER_DN_SUFFIX = "ou=People,dc=jboss,dc=org";

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

    public static LDAPConfiguration getConfiguration() {
        LDAPConfigurationBuilder builder = new LDAPConfigurationBuilder();
        LDAPConfiguration config = (LDAPConfiguration) builder.build();

        config.setBindDN("uid=admin,ou=system").setBindCredential("secret").setLdapURL(LDAP_URL);
        config.setUserDNSuffix(USER_DN_SUFFIX).setRoleDNSuffix(ROLES_DN_SUFFIX);
        config.setGroupDNSuffix(GROUP_DN_SUFFIX);

        return config;
    }

    @Override
    public void importLDIF(String fileName) throws Exception {
        super.importLDIF(fileName);
    }
}
