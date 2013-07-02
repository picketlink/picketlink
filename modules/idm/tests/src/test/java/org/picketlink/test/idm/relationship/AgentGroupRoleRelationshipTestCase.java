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
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * <p>
 * Test case for the relationship between {@link Agent}, {@link Group} and {@link Role} types.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class AgentGroupRoleRelationshipTestCase<T extends Agent> extends AbstractIdentityManagerTestCase {

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
        return (T) getIdentityManager().getAgent("someAgent");
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

        PartitionManager partitionManager = getPartitionManager();
        IdentityManager identityManager = getIdentityManager();

        // developerAgent is an employee at the company group
        partitionManager.grantGroupRole(developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        partitionManager.grantGroupRole(developerAgent, developerRole, projectGroup);

        // projectManagerAgent is an employee at the company group
        partitionManager.grantGroupRole(projectManagerAgent, employeeRole, companyGroup);

        // projectManagerAgent is the manager of the project group
        partitionManager.grantGroupRole(projectManagerAgent, managerRole, projectGroup);

        assertTrue(partitionManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
        assertTrue(partitionManager.hasGroupRole(developerAgent, developerRole, projectGroup));

        assertTrue(partitionManager.hasGroupRole(projectManagerAgent, employeeRole, companyGroup));
        assertTrue(partitionManager.hasGroupRole(projectManagerAgent, managerRole, projectGroup));

        assertFalse(partitionManager.hasGroupRole(developerAgent, managerRole, projectGroup));
        assertFalse(partitionManager.hasGroupRole(projectManagerAgent, developerRole, projectGroup));

        assertFalse(partitionManager.isMember(developerAgent, projectGroup));
        assertFalse(partitionManager.isMember(developerAgent, companyGroup));
        assertFalse(partitionManager.hasRole(developerAgent, employeeRole));
    }

    @Test
    public void testGrantParentGroupRole() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group administratorsGroup = createGroup("Administrators", null);

        Group systemAdministradorsGroup = createGroupWithParent("System Administrators", administratorsGroup);

        Group databaseAdministratorsGroup = createGroupWithParent("Database Administrators", systemAdministradorsGroup);

        Role managerRole = createRole("Administrators Manager");

        T agent = createIdentityType("agent", null);

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.grantGroupRole(agent, managerRole, administratorsGroup);

        assertTrue(partitionManager.hasGroupRole(agent, managerRole, administratorsGroup));
        assertTrue(partitionManager.hasGroupRole(agent, managerRole, databaseAdministratorsGroup));
        assertTrue(partitionManager.hasGroupRole(agent, managerRole, systemAdministradorsGroup));

        Role securityManager = createRole("Data Security Manager");

        partitionManager.grantGroupRole(agent, securityManager, databaseAdministratorsGroup);

        assertTrue(partitionManager.hasGroupRole(agent, securityManager, databaseAdministratorsGroup));
        assertFalse(partitionManager.hasGroupRole(agent, securityManager, administratorsGroup));
        assertFalse(partitionManager.hasGroupRole(agent, securityManager, systemAdministradorsGroup));

        partitionManager.revokeGroupRole(agent, managerRole, administratorsGroup);

        assertFalse(partitionManager.hasGroupRole(agent, managerRole, administratorsGroup));
        assertFalse(partitionManager.hasGroupRole(agent, managerRole, databaseAdministratorsGroup));
        assertFalse(partitionManager.hasGroupRole(agent, managerRole, systemAdministradorsGroup));

        partitionManager.grantGroupRole(agent, managerRole, systemAdministradorsGroup);

        assertTrue(partitionManager.hasGroupRole(agent, managerRole, databaseAdministratorsGroup));
        assertTrue(partitionManager.hasGroupRole(agent, managerRole, systemAdministradorsGroup));
        assertFalse(partitionManager.hasGroupRole(agent, managerRole, administratorsGroup));
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

        PartitionManager partitionManager = getPartitionManager();
        IdentityManager identityManager = getIdentityManager();

        // developerAgent is an employee at the company group
        partitionManager.grantGroupRole(developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        partitionManager.grantGroupRole(developerAgent, developerRole, projectGroup);

        assertTrue(partitionManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
        assertTrue(partitionManager.hasGroupRole(developerAgent, developerRole, projectGroup));

        partitionManager.revokeGroupRole(developerAgent, developerRole, projectGroup);

        assertFalse(partitionManager.hasGroupRole(developerAgent, developerRole, projectGroup));
        assertTrue(partitionManager.hasGroupRole(developerAgent, employeeRole, companyGroup));

        partitionManager.revokeGroupRole(developerAgent, employeeRole, companyGroup);

        assertFalse(partitionManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
    }

}
