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
import org.picketlink.idm.file.internal.FileIdentityStoreConfiguration;
import org.picketlink.idm.file.internal.FilePartitionStoreConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.test.idm.AgentGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.AgentGroupsRelationshipTestCase;
import org.picketlink.test.idm.AgentManagementTestCase;
import org.picketlink.test.idm.AgentQueryTestCase;
import org.picketlink.test.idm.AgentRolesRelationshipTestCase;
import org.picketlink.test.idm.ApplicationUserRelationshipTestCase;
import org.picketlink.test.idm.CertificateCredentialTestCase;
import org.picketlink.test.idm.GroupManagementTestCase;
import org.picketlink.test.idm.GroupMembershipTestCase;
import org.picketlink.test.idm.GroupQueryTestCase;
import org.picketlink.test.idm.PasswordCredentialTestCase;
import org.picketlink.test.idm.RealmManagementTestCase;
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
@SuiteClasses({ RealmManagementTestCase.class, GroupMembershipTestCase.class, ApplicationUserRelationshipTestCase.class, UserManagementTestCase.class, AgentManagementTestCase.class, RoleManagementTestCase.class,
        GroupManagementTestCase.class, AgentGroupsRelationshipTestCase.class,
        UserRolesRelationshipTestCase.class, AgentRolesRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class,
        AgentGroupRoleRelationshipTestCase.class, RoleQueryTestCase.class, GroupQueryTestCase.class, UserQueryTestCase.class,
        AgentQueryTestCase.class, PasswordCredentialTestCase.class, CertificateCredentialTestCase.class})
public class FileIdentityStoreTestSuite implements TestLifecycle {

    private static FileIdentityStoreTestSuite instance;
    private IdentityManager identityManager;

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
        if (this.identityManager == null) {
            IdentityConfiguration config = new IdentityConfiguration();

            config.addStoreConfiguration(getConfiguration());
            config.addStoreConfiguration(getConfiguration2());
            config.addStoreConfiguration(new FilePartitionStoreConfiguration());

            this.identityManager = new DefaultIdentityManager();

            identityManager.bootstrap(config, new DefaultIdentityStoreInvocationContextFactory(null));
        }

        return this.identityManager;
    }

    @Override
    public void onDestroy() {

    }

    public static FileIdentityStoreConfiguration getConfiguration() {
        return new FileIdentityStoreConfiguration();
    }
    
    public static FileIdentityStoreConfiguration getConfiguration2() {
        FileIdentityStoreConfiguration config = new FileIdentityStoreConfiguration();
        
        config.setRealm("Testing");
        
        return config;
    }

}
