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
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.User;

/**
 * <p>Test case for the relationship between {@link User} and {@link Group} types.
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
        User someUser = getUser("someUser");
        
        Role managerRole = new SimpleRole("manager");
        
        if (getIdentityManager().getRole(managerRole.getName()) != null) {
            getIdentityManager().remove(managerRole);
        }
        
        getIdentityManager().add(managerRole);

        Role developerRole = new SimpleRole("developer");
        
        if (getIdentityManager().getRole(developerRole.getName()) != null) {
            getIdentityManager().remove(developerRole);
        }
        
        getIdentityManager().add(developerRole);

        Group salesGroup = new SimpleGroup("sales");
        
        if (getIdentityManager().getGroup(salesGroup.getName()) != null) {
            getIdentityManager().remove(salesGroup);
        }
        
        getIdentityManager().add(salesGroup);

        Group employeeGroup = new SimpleGroup("employee");
        
        if (getIdentityManager().getGroup(employeeGroup.getName()) != null) {
            getIdentityManager().remove(employeeGroup);
        }

        getIdentityManager().add(employeeGroup);
        
        getIdentityManager().grantGroupRole(someUser, managerRole, salesGroup);
        getIdentityManager().grantGroupRole(someUser, developerRole, employeeGroup);
        
        assertTrue(getIdentityManager().hasGroupRole(someUser, managerRole, salesGroup));
        assertTrue(getIdentityManager().hasGroupRole(someUser, developerRole, employeeGroup));
        
        assertFalse(getIdentityManager().hasGroupRole(someUser, developerRole, salesGroup));
    }
    
    /**
     * <p>Tests revoking a {@link GroupRole}.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testRevokeGroupRole() throws Exception {
        User someUser = getUser("someUser");
        
        Role someRole = new SimpleRole("manager");
        
        if (getIdentityManager().getRole(someRole.getName()) != null) {
            getIdentityManager().remove(someRole);
        }
        
        getIdentityManager().add(someRole);
        
        Group someGroup = new SimpleGroup("sales");
        
        if (getIdentityManager().getGroup(someGroup.getName()) != null) {
            getIdentityManager().remove(someGroup);
        }
        
        getIdentityManager().add(someGroup);
        
        getIdentityManager().grantGroupRole(someUser, someRole, someGroup);
        
        assertTrue(getIdentityManager().hasGroupRole(someUser, someRole, someGroup));
        
        getIdentityManager().revokeGroupRole(someUser, someRole, someGroup);
        
        assertFalse(getIdentityManager().hasGroupRole(someUser, someRole, someGroup));
    }
    
}
