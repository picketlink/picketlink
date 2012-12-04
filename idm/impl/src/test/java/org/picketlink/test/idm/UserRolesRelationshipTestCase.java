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

import junit.framework.Assert;

import org.junit.Test;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
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
        SimpleUser user = new SimpleUser("someUser");
        
        getIdentityManager().add(user);
        
        user.setEnabled(true);
        
        getIdentityManager().update(user);
        
        SimpleRole role = new SimpleRole("someRole");
        
        getIdentityManager().add(role);
        
        getIdentityManager().grantRole(user, role);
        
        Assert.assertTrue(getIdentityManager().hasRole(user, role));
    }
    
}
