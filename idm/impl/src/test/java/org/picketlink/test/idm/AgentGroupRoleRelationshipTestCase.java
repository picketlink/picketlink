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

package org.picketlink.test.idm;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Role;

/**
 * <p>
 * Test case for the relationship between {@link Agent}, {@link Group} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentGroupRoleRelationshipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests adding an {@link Agent} as a member of a {@link Group} with a specific {@link Role}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGrantGroupRole() throws Exception {
        Agent developerAgent = createAgent("developerAgent");
        Agent projectManagerAgent = createAgent("projectManagerAgent");

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
        Agent developerAgent = createAgent("developerAgent");

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
    }

}
