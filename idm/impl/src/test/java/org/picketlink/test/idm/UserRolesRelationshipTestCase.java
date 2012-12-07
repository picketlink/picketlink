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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.User;

/**
 * <p>Test case for the relationship between {@link User} and {@link Role} types.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class UserRolesRelationshipTestCase extends AbstractIdentityManagerTestCase {

    /**
     * <p>Tests granting roles to users.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testGrantRoleToUser() throws Exception {
        User someUser = getUser("someUser");
        
        Role someRole = new SimpleRole("someRole");
        
        getIdentityManager().add(someRole);
        
        getIdentityManager().grantRole(someUser, someRole);
        
        assertTrue(getIdentityManager().hasRole(someUser, someRole));
        
        User someAnotherUser = getUser("someAnotherUser");
        
        assertFalse(getIdentityManager().hasRole(someAnotherUser, someRole));
        
        Role someAnotherRole = new SimpleRole("someAnotherRole");
        
        getIdentityManager().add(someAnotherRole);
        
        getIdentityManager().grantRole(someAnotherUser, someAnotherRole);
        
        assertTrue(getIdentityManager().hasRole(someAnotherUser, someAnotherRole));
    }
    
    /**
     * <p>Tests granting multiple roles to users.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testGrantMultipleRolesToUser() throws Exception {
        User someUser = getUser("someUser");
        
        Role someRole = new SimpleRole("someRole");
        Role someAnotherRole = new SimpleRole("someAnotherRole");
        
        getIdentityManager().add(someRole);
        getIdentityManager().add(someAnotherRole);
        
        getIdentityManager().grantRole(someUser, someRole);
        getIdentityManager().grantRole(someUser, someAnotherRole);
        
        assertTrue(getIdentityManager().hasRole(someUser, someRole));
        assertTrue(getIdentityManager().hasRole(someUser, someAnotherRole));
    }
    
    /**
     * <p>Tests revoking oles from users.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testRevokeRoleFromUser() throws Exception {
        User someUser = getUser("someUser");
        
        Role someRole = new SimpleRole("someRole");
        Role someAnotherRole = new SimpleRole("someAnotherRole");
        
        getIdentityManager().add(someRole);
        getIdentityManager().add(someAnotherRole);
        
        getIdentityManager().grantRole(someUser, someRole);
        getIdentityManager().grantRole(someUser, someAnotherRole);
        
        assertTrue(getIdentityManager().hasRole(someUser, someRole));
        assertTrue(getIdentityManager().hasRole(someUser, someAnotherRole));
        
        getIdentityManager().revokeRole(someUser, someRole);
        
        assertFalse(getIdentityManager().hasRole(someUser, someRole));
        assertTrue(getIdentityManager().hasRole(someUser, someAnotherRole));
    }
    
}
