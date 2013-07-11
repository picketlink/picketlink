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
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.IdentityConfigurationTestVisitor;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * <p>
 * Test case for the relationship between {@link Agent}, {@link Group} and {@link Role} types.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class AgentGroupRoleRelationshipTestCase<T extends Agent> extends AbstractPartitionManagerTestCase {

    public AgentGroupRoleRelationshipTestCase(IdentityConfigurationTestVisitor builder) {
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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        IdentityManager identityManager = getIdentityManager();

        // developerAgent is an employee at the company group
        relationshipManager.grantGroupRole(developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        relationshipManager.grantGroupRole(developerAgent, developerRole, projectGroup);

        // projectManagerAgent is an employee at the company group
        relationshipManager.grantGroupRole(projectManagerAgent, employeeRole, companyGroup);

        // projectManagerAgent is the manager of the project group
        relationshipManager.grantGroupRole(projectManagerAgent, managerRole, projectGroup);

        assertTrue(relationshipManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
        assertTrue(relationshipManager.hasGroupRole(developerAgent, developerRole, projectGroup));

        assertTrue(relationshipManager.hasGroupRole(projectManagerAgent, employeeRole, companyGroup));
        assertTrue(relationshipManager.hasGroupRole(projectManagerAgent, managerRole, projectGroup));

        assertFalse(relationshipManager.hasGroupRole(developerAgent, managerRole, projectGroup));
        assertFalse(relationshipManager.hasGroupRole(projectManagerAgent, developerRole, projectGroup));

        assertFalse(relationshipManager.isMember(developerAgent, projectGroup));
        assertFalse(relationshipManager.isMember(developerAgent, companyGroup));
        assertFalse(relationshipManager.hasRole(developerAgent, employeeRole));
    }

    @Test
    public void testGrantParentGroupRole() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group administratorsGroup = createGroup("Administrators", null);

        Group systemAdministradorsGroup = createGroupWithParent("System Administrators", administratorsGroup);

        Group databaseAdministratorsGroup = createGroupWithParent("Database Administrators", systemAdministradorsGroup);

        Role managerRole = createRole("Administrators Manager");

        T agent = createIdentityType("agent", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.grantGroupRole(agent, managerRole, administratorsGroup);

        assertTrue(relationshipManager.hasGroupRole(agent, managerRole, administratorsGroup));
        assertTrue(relationshipManager.hasGroupRole(agent, managerRole, databaseAdministratorsGroup));
        assertTrue(relationshipManager.hasGroupRole(agent, managerRole, systemAdministradorsGroup));

        Role securityManager = createRole("Data Security Manager");

        relationshipManager.grantGroupRole(agent, securityManager, databaseAdministratorsGroup);

        assertTrue(relationshipManager.hasGroupRole(agent, securityManager, databaseAdministratorsGroup));
        assertFalse(relationshipManager.hasGroupRole(agent, securityManager, administratorsGroup));
        assertFalse(relationshipManager.hasGroupRole(agent, securityManager, systemAdministradorsGroup));

        relationshipManager.revokeGroupRole(agent, managerRole, administratorsGroup);

        assertFalse(relationshipManager.hasGroupRole(agent, managerRole, administratorsGroup));
        assertFalse(relationshipManager.hasGroupRole(agent, managerRole, databaseAdministratorsGroup));
        assertFalse(relationshipManager.hasGroupRole(agent, managerRole, systemAdministradorsGroup));

        relationshipManager.grantGroupRole(agent, managerRole, systemAdministradorsGroup);

        assertTrue(relationshipManager.hasGroupRole(agent, managerRole, databaseAdministratorsGroup));
        assertTrue(relationshipManager.hasGroupRole(agent, managerRole, systemAdministradorsGroup));
        assertFalse(relationshipManager.hasGroupRole(agent, managerRole, administratorsGroup));
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
        relationshipManager.grantGroupRole(developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        relationshipManager.grantGroupRole(developerAgent, developerRole, projectGroup);

        assertTrue(relationshipManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
        assertTrue(relationshipManager.hasGroupRole(developerAgent, developerRole, projectGroup));

        relationshipManager.revokeGroupRole(developerAgent, developerRole, projectGroup);

        assertFalse(relationshipManager.hasGroupRole(developerAgent, developerRole, projectGroup));
        assertTrue(relationshipManager.hasGroupRole(developerAgent, employeeRole, companyGroup));

        relationshipManager.revokeGroupRole(developerAgent, employeeRole, companyGroup);

        assertFalse(relationshipManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
    }

}
