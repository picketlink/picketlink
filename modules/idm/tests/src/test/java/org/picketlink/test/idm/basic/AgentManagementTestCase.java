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

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.SampleModel;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for the {@link Agent} basic management operations using only the default realm.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AgentManagementTestCase extends AbstractIdentityTypeTestCase<Agent> {

    public AgentManagementTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testCreate() throws Exception {
        Agent newAgent = createIdentityType();
        Agent storedAgent = getAgent(newAgent.getLoginName());

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
    @IgnoreTester(LDAPStoreConfigurationTester.class)
    public void testUpdate() throws Exception {
        Agent storedAgent = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        Date actualDate = new Date();
        
        storedAgent.setExpirationDate(actualDate);
        storedAgent.setAttribute(new Attribute<String>("someAttribute", "1"));
        
        identityManager.update(storedAgent);

        Agent updatedUser = getAgent(storedAgent.getLoginName());

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

        Agent removedUserInstance = getAgent(someAgent.getLoginName());

        assertNull(removedUserInstance);
        
        anotherAgent = getAgent(anotherAgent.getLoginName());
        
        assertNotNull(anotherAgent);
        
        Role role = createRole("role");
        Group group = createGroup("group", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        SampleModel.grantRole(relationshipManager, anotherAgent, role);
        SampleModel.addToGroup(relationshipManager, anotherAgent, group);

        RelationshipQuery<?> relationshipQuery = relationshipManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ASSIGNEE, anotherAgent);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.MEMBER, anotherAgent);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        identityManager.remove(anotherAgent);

        relationshipQuery = relationshipManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ASSIGNEE, anotherAgent);

        assertTrue(relationshipQuery.getResultList().isEmpty());

        relationshipQuery = relationshipManager.createRelationshipQuery(GroupMembership.class);

        relationshipQuery.setParameter(GroupMembership.MEMBER, anotherAgent);

        assertTrue(relationshipQuery.getResultList().isEmpty());
    }

    @Override
    protected Agent getIdentityType() {
        return getAgent("someSimpleAgent");
    }

    @Override
    protected Agent createIdentityType() {
        return createAgent("someSimpleAgent");
    }

    @Test
    public void testEqualsMethod() {
        Agent instanceA = createAgent("agentA");
        Agent instanceB = createAgent("agentB");
        
        assertFalse(instanceA.equals(instanceB));
        
        assertTrue(instanceA.getLoginName().equals(getAgent(instanceA.getLoginName()).getLoginName()));
    }

}
