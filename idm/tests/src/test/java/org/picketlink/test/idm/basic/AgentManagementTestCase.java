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

package org.picketlink.test.idm.basic;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityTypeTestCase;

/**
 * <p>
 * Test case for the {@link Agent} basic management operations using only the default realm.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentManagementTestCase extends AbstractIdentityTypeTestCase<Agent> {

    @Test
    public void testCreate() throws Exception {
        Agent newAgent = createIdentityType();
        
        IdentityManager identityManager = getIdentityManager();

        Agent storedAgent = identityManager.getAgent(newAgent.getLoginName());

        assertNotNull(storedAgent);
        assertEquals(newAgent.getId(), storedAgent.getId());
        assertEquals(newAgent.getLoginName(), storedAgent.getLoginName());
        assertTrue(storedAgent.isEnabled());
        assertNotNull(storedAgent.getPartition());
        assertEquals(Realm.DEFAULT_REALM, storedAgent.getPartition().getName());
        assertTrue(storedAgent.isEnabled());
        assertNull(storedAgent.getExpirationDate());
        assertNotNull(storedAgent.getCreatedDate());
        assertTrue(new Date().compareTo(storedAgent.getCreatedDate()) >= 0);
    }

    @Test
    public void testUpdate() throws Exception {
        Agent storedAgent = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        Date actualDate = new Date();
        
        storedAgent.setExpirationDate(actualDate);
        storedAgent.setAttribute(new Attribute<String>("someAttribute", "1"));
        
        identityManager.update(storedAgent);

        Agent updatedUser = identityManager.getAgent(storedAgent.getLoginName());

        assertNotNull(updatedUser.getAttribute("someAttribute"));
        assertEquals("1", updatedUser.getAttribute("someAttribute").getValue());
        assertEquals(actualDate, updatedUser.getExpirationDate());
    }

    @Test
    public void testRemove() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Agent someAgent = createIdentityType();
        Agent anotherAgent = createAgent("someAnotherAgent");

        identityManager.remove(someAgent);

        Agent removedUserInstance = getIdentityManager().getAgent(someAgent.getLoginName());

        assertNull(removedUserInstance);
        
        anotherAgent = identityManager.getAgent(anotherAgent.getLoginName());
        
        assertNotNull(anotherAgent);
        
        Role role = createRole("role");
        Group group = createGroup("group", null);
        
        identityManager.grantRole(anotherAgent, role);
        identityManager.addToGroup(anotherAgent, group);
        identityManager.grantGroupRole(anotherAgent, role, group);
        
        RelationshipQuery<?> relationshipQuery = identityManager.createRelationshipQuery(Grant.class);
        
        relationshipQuery.setParameter(Grant.ASSIGNEE, anotherAgent);
        
        assertFalse(relationshipQuery.getResultList().isEmpty());
        
        relationshipQuery = identityManager.createRelationshipQuery(GroupMembership.class);
        
        relationshipQuery.setParameter(GroupMembership.MEMBER, anotherAgent);
        
        assertFalse(relationshipQuery.getResultList().isEmpty());
        
        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);
        
        relationshipQuery.setParameter(GroupRole.MEMBER, anotherAgent);
        
        assertFalse(relationshipQuery.getResultList().isEmpty());
        
        identityManager.remove(anotherAgent);
        
        relationshipQuery = identityManager.createRelationshipQuery(Grant.class);
        
        relationshipQuery.setParameter(Grant.ASSIGNEE, anotherAgent);
        
        assertTrue(relationshipQuery.getResultList().isEmpty());
        
        relationshipQuery = identityManager.createRelationshipQuery(GroupMembership.class);
        
        relationshipQuery.setParameter(GroupMembership.MEMBER, anotherAgent);
        
        assertTrue(relationshipQuery.getResultList().isEmpty());
        
        relationshipQuery = identityManager.createRelationshipQuery(GroupRole.class);
        
        relationshipQuery.setParameter(GroupRole.MEMBER, anotherAgent);
        
        assertTrue(relationshipQuery.getResultList().isEmpty());
    }

    @Override
    protected Agent getIdentityType() {
        return getIdentityManager().getAgent("someAgent");
    }

    @Override
    protected Agent createIdentityType() {
        return createAgent("someAgent");
    }

}
