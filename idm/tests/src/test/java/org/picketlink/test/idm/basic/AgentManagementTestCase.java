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
