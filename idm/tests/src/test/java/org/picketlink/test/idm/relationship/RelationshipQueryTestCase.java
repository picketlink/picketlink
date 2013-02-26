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

package org.picketlink.test.idm.relationship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * @author Pedro Silva
 *
 */
public class RelationshipQueryTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testFindAllRelationshipsForUser() throws Exception {
        User user = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group");
        
        IdentityManager identityManager = getIdentityManager();
        
        identityManager.grantRole(user, role);
        identityManager.grantGroupRole(user, role, group);
        identityManager.addToGroup(user, group);
        
        RelationshipQuery<Relationship> query = identityManager.createRelationshipQuery(Relationship.class);
        
        query.setParameter(Relationship.IDENTITY, user);
        
        List<Relationship> result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
        
        query = identityManager.createRelationshipQuery(Relationship.class);
        
        query.setParameter(Relationship.IDENTITY, role);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        
        query = identityManager.createRelationshipQuery(Relationship.class);
        
        query.setParameter(Relationship.IDENTITY, group);
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
        
        query = identityManager.createRelationshipQuery(Relationship.class);
        
        query.setParameter(Relationship.IDENTITY, user.getId());
        
        result = query.getResultList();
        
        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
    }
    
}