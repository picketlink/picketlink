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
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Test case for the relationship between {@link User}, {@link Group} and {@link Role} types.
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class UserGroupRoleRelationshipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>
     * Tests adding an {@link User} as a member of a {@link Group} with a specific {@link Role}.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testGrantGroupRole() throws Exception {
        User developerUser = createUser("developerUser");
        User projectManagerUser = createUser("projectManagerUser");

        Role managerRole = createRole("Manager");
        Role developerRole = createRole("Developer");
        Role employeeRole = createRole("Employee");

        Group companyGroup = createGroup("Company Group", null);
        Group projectGroup = createGroup("Project Group", null);

        IdentityManager identityManager = getIdentityManager();

        // developerUser is an employee at the company group
        identityManager.grantGroupRole(developerUser, employeeRole, companyGroup);

        // developerUser is a developer at the project group
        identityManager.grantGroupRole(developerUser, developerRole, projectGroup);

        // projectManagerUser is an employee at the company group
        identityManager.grantGroupRole(projectManagerUser, employeeRole, companyGroup);

        // projectManagerUser is the manager of the project group
        identityManager.grantGroupRole(projectManagerUser, managerRole, projectGroup);

        assertTrue(identityManager.hasGroupRole(developerUser, employeeRole, companyGroup));
        assertTrue(identityManager.hasGroupRole(developerUser, developerRole, projectGroup));

        assertTrue(identityManager.hasGroupRole(projectManagerUser, employeeRole, companyGroup));
        assertTrue(identityManager.hasGroupRole(projectManagerUser, managerRole, projectGroup));

        assertFalse(identityManager.hasGroupRole(developerUser, managerRole, projectGroup));
        assertFalse(identityManager.hasGroupRole(projectManagerUser, developerRole, projectGroup));
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
        User developerUser = createUser("developerUser");

        Role developerRole = createRole("Developer");
        Role employeeRole = createRole("Employee");

        Group companyGroup = createGroup("Company Group", null);
        Group projectGroup = createGroup("Project Group", null);

        IdentityManager identityManager = getIdentityManager();

        // developerUser is an employee at the company group
        identityManager.grantGroupRole(developerUser, employeeRole, companyGroup);

        // developerUser is a developer at the project group
        identityManager.grantGroupRole(developerUser, developerRole, projectGroup);

        assertTrue(identityManager.hasGroupRole(developerUser, employeeRole, companyGroup));
        assertTrue(identityManager.hasGroupRole(developerUser, developerRole, projectGroup));

        identityManager.revokeGroupRole(developerUser, developerRole, projectGroup);

        assertFalse(identityManager.hasGroupRole(developerUser, developerRole, projectGroup));
    }

}
