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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.idm.IDMType;
import org.picketlink.config.idm.StoreConfigurationType;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.internal.XMLBasedIdentityManagerProvider;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStoreConfiguration;
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
import org.picketlink.test.idm.relationship.AgentGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupsRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupMembershipTestCase;
import org.picketlink.test.idm.relationship.UserGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.relationship.UserRolesRelationshipTestCase;

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
        UserRolesRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class, GroupMembershipTestCase.class
})
public class LDAPIdentityStoreTestSuite extends AbstractLDAPTest implements TestLifecycle {

    private static LDAPIdentityStoreTestSuite instance;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new LDAPIdentityStoreTestSuite();
        }

        return instance;
    }

    private static final String DEFAULT_IDENTITY_CONFIG_FILE = "config/embedded-ldap-config.xml";

    private String identityConfigFile;

    @BeforeClass
    public static void onBeforeClass() {
        try {
            init();
            instance.setup();
            instance.overrideProperties();
            String ldifFile =  System.getProperty("plidm.ldif.file", "ldap/users.ldif");
            instance.importLDIF(ldifFile);
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

    @Before
    @Override
    public void setup() throws Exception {
        identityConfigFile = System.getProperty("plidm.xml.configuration", DEFAULT_IDENTITY_CONFIG_FILE);

        // Setup and start Ldap only in case of embedded ApacheDS
        if (DEFAULT_IDENTITY_CONFIG_FILE.equals(identityConfigFile)) {
            super.setup();
        }
    }

    @After
    @Override
    public void tearDown() throws Exception {
        // Stop Ldap only in case of embedded ApacheDS
        if (DEFAULT_IDENTITY_CONFIG_FILE.equals(identityConfigFile)) {
            super.tearDown();
        }
    }

    /**
     * Override properties needed for LDIF import
     */
    private void overrideProperties() {
        XMLBasedIdentityManagerProvider configProvider = new XMLBasedIdentityManagerProvider();
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(identityConfigFile);
        IDMType idmType = configProvider.parseIDMType(configStream);
        StoreConfigurationType storeType = idmType.getIdentityConfigurationType().getIdentityStoreConfigurations().get(0);

        adminDN = (String)storeType.getProperty("bindDN");
        adminPW = (String)storeType.getProperty("bindCredential");
        dn = (String)storeType.getProperty("baseDN");

        // Parse host and port from string like "ldap://localhost:1389"
        String ldapURL = (String)storeType.getProperty("ldapURL");
        String[] splits = ldapURL.split(":");
        serverHost = splits[1].substring(2);
        port = splits[2];
    }

    @Override
    public void onInit() {

    }

    @Override
    public IdentityManager createIdentityManager() {
        XMLBasedIdentityManagerProvider configProvider = new XMLBasedIdentityManagerProvider();
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(identityConfigFile);
        return configProvider.buildIdentityManager(configStream);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void importLDIF(String fileName) throws Exception {
        if (DEFAULT_IDENTITY_CONFIG_FILE.equals(identityConfigFile)) {
            super.importLDIF(fileName);
        } else {
            // TODO: Find a way to perform LDIF import for non-embedded LDAP servers (CMD via Runtime.getRuntime ?)
        }
    }
}
