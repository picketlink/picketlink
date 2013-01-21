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

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.file.internal.FileDataSource;
import org.picketlink.idm.file.internal.FileIdentityStoreConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
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
import org.picketlink.test.idm.relationship.AgentGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupsRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentRolesRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupMembershipTestCase;
import org.picketlink.test.idm.relationship.UserGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.relationship.UserRolesRelationshipTestCase;
import org.picketlink.test.idm.usecases.ApplicationUserRelationshipTestCase;

/**
 * <p>
 * Test suite for the {@link IdentityManager} using a {@link LDAPIdentityStore}.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ RealmManagementTestCase.class, TierManagementTestCase.class, GroupMembershipTestCase.class,
    ApplicationUserRelationshipTestCase.class, UserManagementTestCase.class, AgentManagementTestCase.class,
    RoleManagementTestCase.class, GroupManagementTestCase.class, AgentGroupsRelationshipTestCase.class,
    UserRolesRelationshipTestCase.class, AgentRolesRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class,
    AgentGroupRoleRelationshipTestCase.class, RoleQueryTestCase.class, GroupQueryTestCase.class, UserQueryTestCase.class,
    AgentQueryTestCase.class, PasswordCredentialTestCase.class, CertificateCredentialTestCase.class, DigestCredentialTestCase.class })
public class FileIdentityStoreTestSuite implements TestLifecycle {

    private static FileIdentityStoreTestSuite instance;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new FileIdentityStoreTestSuite();
        }

        return instance;
    }

    @Override
    public void onInit() {

    }

    @Override
    public IdentityManager createIdentityManager() {

        IdentityConfiguration config = new IdentityConfiguration();

        FileDataSource dataSource = new FileDataSource();

        addDefaultConfiguration(config, dataSource);
        addTestingRealmConfiguration(config, dataSource);

        IdentityManager identityManager = new DefaultIdentityManager();

        identityManager.bootstrap(config, new DefaultIdentityStoreInvocationContextFactory(null));

        return identityManager;
    }

    private void addTestingRealmConfiguration(IdentityConfiguration config, FileDataSource dataSource) {
        FileIdentityStoreConfiguration testingRealmConfiguration = getTestingRealmConfiguration();

        testingRealmConfiguration.setDataSource(dataSource);

        config.addStoreConfiguration(testingRealmConfiguration);
    }

    private void addDefaultConfiguration(IdentityConfiguration config, FileDataSource dataSource) {
        FileIdentityStoreConfiguration defaultConfiguration = getDefaultConfiguration();

        defaultConfiguration.setDataSource(dataSource);

        config.addStoreConfiguration(defaultConfiguration);
    }

    @Override
    public void onDestroy() {

    }

    public static FileIdentityStoreConfiguration getDefaultConfiguration() {
        return new FileIdentityStoreConfiguration();
    }

    public static FileIdentityStoreConfiguration getTestingRealmConfiguration() {
        FileIdentityStoreConfiguration config = new FileIdentityStoreConfiguration();

        config.setRealm("Testing");

        return config;
    }

}
