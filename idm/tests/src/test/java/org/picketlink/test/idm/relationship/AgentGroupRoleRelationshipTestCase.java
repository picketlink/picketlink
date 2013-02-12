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

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Role;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

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
        T developerAgent = createIdentityType("developerAgent", null);
        T projectManagerAgent = createIdentityType("projectManagerAgent", null);

        Role managerRole = createRole("Manager");
        Role developerRole = createRole("Developer");
        Role employeeRole = createRole("Employee");

        Group companyGroup = createGroup("Company Group", null);
        Group projectGroup = createGroup("Project Group", null);

        IdentityManager identityManager = getIdentityManager();

        // developerAgent is an employee at the company group
        identityManager.grantGroupRole(developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        identityManager.grantGroupRole(developerAgent, developerRole, projectGroup);

        // projectManagerAgent is an employee at the company group
        identityManager.grantGroupRole(projectManagerAgent, employeeRole, companyGroup);

        // projectManagerAgent is the manager of the project group
        identityManager.grantGroupRole(projectManagerAgent, managerRole, projectGroup);

        assertTrue(identityManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
        assertTrue(identityManager.hasGroupRole(developerAgent, developerRole, projectGroup));

        assertTrue(identityManager.hasGroupRole(projectManagerAgent, employeeRole, companyGroup));
        assertTrue(identityManager.hasGroupRole(projectManagerAgent, managerRole, projectGroup));

        assertFalse(identityManager.hasGroupRole(developerAgent, managerRole, projectGroup));
        assertFalse(identityManager.hasGroupRole(projectManagerAgent, developerRole, projectGroup));
        
        assertTrue(identityManager.hasRole(developerAgent, developerRole));
        assertTrue(identityManager.isMember(developerAgent, projectGroup));
        assertFalse(identityManager.hasRole(developerAgent, managerRole));
        
        assertTrue(identityManager.hasRole(projectManagerAgent, managerRole));
        assertTrue(identityManager.isMember(projectManagerAgent, projectGroup));
        assertFalse(identityManager.hasRole(projectManagerAgent, developerRole));
        
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

        IdentityManager identityManager = getIdentityManager();

        // developerAgent is an employee at the company group
        identityManager.grantGroupRole(developerAgent, employeeRole, companyGroup);

        // developerAgent is a developer at the project group
        identityManager.grantGroupRole(developerAgent, developerRole, projectGroup);

        assertTrue(identityManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
        assertTrue(identityManager.hasGroupRole(developerAgent, developerRole, projectGroup));

        identityManager.revokeGroupRole(developerAgent, developerRole, projectGroup);

        assertFalse(identityManager.hasGroupRole(developerAgent, developerRole, projectGroup));
        assertTrue(identityManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
        
        identityManager.revokeGroupRole(developerAgent, employeeRole, companyGroup);
        
        assertFalse(identityManager.hasGroupRole(developerAgent, employeeRole, companyGroup));
    }

}
