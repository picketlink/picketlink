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
 * <p>Test case for the relationship between {@link User}, {@link Group} and {@link Role} types.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class UserGroupRoleRelationshipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>Tests adding an {@link User} as a member of a {@link Group} with a specific {@link Role}.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testGrantGroupRole() throws Exception {
        User someUser = loadOrCreateUser("someUser", true);
        
        Role managerRole = loadOrCreateRole("manager", true);
        Role developerRole = loadOrCreateRole("developer", true);
        
        Group salesGroup = loadOrCreateGroup("sales", null, true);
        Group employeeGroup = loadOrCreateGroup("employee", null, true);
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.grantGroupRole(someUser, managerRole, salesGroup);
        identityManager.grantGroupRole(someUser, developerRole, employeeGroup);
        
        assertTrue(identityManager.hasGroupRole(someUser, managerRole, salesGroup));
        assertTrue(identityManager.hasGroupRole(someUser, developerRole, employeeGroup));
        
        assertFalse(identityManager.hasGroupRole(someUser, developerRole, salesGroup));
    }
    
    /**
     * <p>Tests adding an {@link User} as a member of a {@link Group} with a specific {@link Role}.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testGrantParentGroupRole() throws Exception {
        User mary = loadOrCreateUser("mary", true);
        User john = loadOrCreateUser("john", true);
        
        Group managerGroup = loadOrCreateGroup("Managers", "Executives", true);
        Group executiveGroup = managerGroup.getParentGroup();
        Role secretaryRole = loadOrCreateRole("secretary", true);
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.grantGroupRole(mary, secretaryRole, managerGroup);
        identityManager.grantGroupRole(john, secretaryRole, executiveGroup);
        
        assertTrue(identityManager.hasGroupRole(mary, secretaryRole, managerGroup));
        assertTrue(identityManager.hasGroupRole(john, secretaryRole, executiveGroup));
        
        assertFalse(identityManager.hasGroupRole(mary, secretaryRole, executiveGroup));
        assertFalse(identityManager.hasGroupRole(john, secretaryRole, managerGroup));
    }
    
    /**
     * <p>Tests revoking a {@link GroupRole}.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testRevokeGroupRole() throws Exception {
        User someUser = loadOrCreateUser("someUser", true);
        Role managerRole = loadOrCreateRole("manager", true);
        Group salesGroup = loadOrCreateGroup("sales", null, true);

        IdentityManager identityManager = getIdentityManager();
        
        identityManager.grantGroupRole(someUser, managerRole, salesGroup);
        
        assertTrue(identityManager.hasGroupRole(someUser, managerRole, salesGroup));
        
        identityManager.revokeGroupRole(someUser, managerRole, salesGroup);
        
        assertFalse(identityManager.hasGroupRole(someUser, managerRole, salesGroup));
    }
    
}
