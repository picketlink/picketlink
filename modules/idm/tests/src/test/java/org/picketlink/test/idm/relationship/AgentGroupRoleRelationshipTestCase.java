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

package org.picketlink.test.idm.relationship;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupRole;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * <p>
 * Test case for the relationship between {@link Agent}, {@link Group} and {@link Role} types.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        SingleConfigLDAPJPAStoreConfigurationTester.class,
        LDAPUserGroupJPARoleConfigurationTester.class})
public class AgentGroupRoleRelationshipTestCase<T extends Agent> extends AbstractPartitionManagerTestCase {

    public AgentGroupRoleRelationshipTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    protected T createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someAgent";
        }

        return (T) createAgent(name, partition);
    }

    protected T createIdentityType(String name) {
        return (T) createAgent(name, null);
    }

    protected T getIdentityType() {
        return (T) getAgent("someAgent");
    }

    /**
     * <p>
     * Tests adding an {@link Agent} as a member of a {@link Group} with a specific {@link Role}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testGrantGroupRole() throws Exception {
        T developerAgent = createIdentityType("developer");
        T projectManagerAgent = createIdentityType("projectManager");

        Role managerRole = createRole("Manager");
        Role developerRole = createRole("Developer");
        Role employeeRole = createRole("Employee");

        Group companyGroup = createGroup("Company Group", null);
        Group projectGroup = createGroup("Project Group", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        IdentityManager identityManager = getIdentityManager();

        // developerAgent is an employee at the company group
        BasicModel.grantGroupRole(relationshipManager, developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        BasicModel.grantGroupRole(relationshipManager, developerAgent, developerRole, projectGroup);

        // projectManagerAgent is an employee at the company group
        BasicModel.grantGroupRole(relationshipManager, projectManagerAgent, employeeRole, companyGroup);

        // projectManagerAgent is the manager of the project group
        BasicModel.grantGroupRole(relationshipManager, projectManagerAgent, managerRole, projectGroup);

        assertTrue(BasicModel.hasGroupRole(relationshipManager, developerAgent, employeeRole, companyGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, developerAgent, developerRole, projectGroup));

        assertTrue(BasicModel.hasGroupRole(relationshipManager, projectManagerAgent, employeeRole, companyGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, projectManagerAgent, managerRole, projectGroup));

        assertFalse(BasicModel.hasGroupRole(relationshipManager, developerAgent, managerRole, projectGroup));
        assertFalse(BasicModel.hasGroupRole(relationshipManager, projectManagerAgent, developerRole, projectGroup));

        assertFalse(BasicModel.isMember(relationshipManager, developerAgent, projectGroup));
        assertFalse(BasicModel.isMember(relationshipManager, developerAgent, companyGroup));
        assertFalse(BasicModel.hasRole(relationshipManager, developerAgent, employeeRole));
    }

    @Test
    @Configuration(exclude = SingleConfigLDAPJPAStoreConfigurationTester.class)
    public void testGrantParentGroupRole() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group administratorsGroup = createGroup("Administrators", null);

        Group systemAdministradorsGroup = createGroupWithParent("System Administrators", administratorsGroup);

        Group databaseAdministratorsGroup = createGroupWithParent("Database Administrators", systemAdministradorsGroup);

        Role managerRole = createRole("Administrators Manager");

        T agent = createIdentityType("agent", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.grantGroupRole(relationshipManager, agent, managerRole, administratorsGroup);

        assertTrue(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, administratorsGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, databaseAdministratorsGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, systemAdministradorsGroup));

        Role securityManager = createRole("Data Security Manager");

        BasicModel.grantGroupRole(relationshipManager, agent, securityManager, databaseAdministratorsGroup);

        assertTrue(BasicModel.hasGroupRole(relationshipManager, agent, securityManager, databaseAdministratorsGroup));
        assertFalse(BasicModel.hasGroupRole(relationshipManager, agent, securityManager, administratorsGroup));
        assertFalse(BasicModel.hasGroupRole(relationshipManager, agent, securityManager, systemAdministradorsGroup));

        BasicModel.revokeGroupRole(relationshipManager, agent, managerRole, administratorsGroup);

        assertFalse(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, administratorsGroup));
        assertFalse(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, databaseAdministratorsGroup));
        assertFalse(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, systemAdministradorsGroup));

        BasicModel.grantGroupRole(relationshipManager, agent, managerRole, systemAdministradorsGroup);

        assertTrue(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, databaseAdministratorsGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, systemAdministradorsGroup));
        assertFalse(BasicModel.hasGroupRole(relationshipManager, agent, managerRole, administratorsGroup));
    }

    /**
     * <p>
     * Tests revoking a {@link GroupRole}.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testRevokeGroupRole() throws Exception {
        T developerAgent = createIdentityType("developerAgent", null);

        Role developerRole = createRole("Developer");
        Role employeeRole = createRole("Employee");

        Group companyGroup = createGroup("Company Group", null);
        Group projectGroup = createGroup("Project Group", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        IdentityManager identityManager = getIdentityManager();

        // developerAgent is an employee at the company group
        BasicModel.grantGroupRole(relationshipManager, developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        BasicModel.grantGroupRole(relationshipManager, developerAgent, developerRole, projectGroup);

        assertTrue(BasicModel.hasGroupRole(relationshipManager, developerAgent, employeeRole, companyGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, developerAgent, developerRole, projectGroup));

        BasicModel.revokeGroupRole(relationshipManager, developerAgent, developerRole, projectGroup);

        assertFalse(BasicModel.hasGroupRole(relationshipManager, developerAgent, developerRole, projectGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, developerAgent, employeeRole, companyGroup));

        BasicModel.revokeGroupRole(relationshipManager, developerAgent, employeeRole, companyGroup);

        assertFalse(BasicModel.hasGroupRole(relationshipManager, developerAgent, employeeRole, companyGroup));
    }

}
