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

package org.picketlink.test.idm.query;

import org.junit.Test;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.picketlink.test.idm.relationship.CustomRelationshipTestCase.CustomRelationship;

/**
 * @author Pedro Silva
 *
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        SingleConfigLDAPJPAStoreConfigurationTester.class, LDAPStoreConfigurationTester.class,
        LDAPUserGroupJPARoleConfigurationTester.class})
public class RelationshipQueryTestCase extends AbstractPartitionManagerTestCase {

    public RelationshipQueryTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    @Configuration (exclude = {LDAPStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
    public void testFindById() throws Exception {
        User user = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        Grant grant = new Grant();

        grant.setAssignee(user);
        grant.setRole(role);

        relationshipManager.add(grant);

        GroupMembership groupMembership = new GroupMembership();

        groupMembership.setGroup(group);
        groupMembership.setMember(user);

        relationshipManager.add(groupMembership);

        RelationshipQuery<? extends Relationship> query = relationshipManager.createRelationshipQuery(Relationship
                .class);

        query.setParameter(Relationship.ID, grant.getId());

        List<? extends Relationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(grant.getId(), result.get(0).getId());

        query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(Relationship.ID, groupMembership.getId());

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(groupMembership.getId(), result.get(0).getId());

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Relationship.ID, groupMembership.getId());

        result = query.getResultList();

        assertTrue(result.isEmpty());
    }

    @Test
    @Configuration (exclude = LDAPStoreConfigurationTester.class)
    public void testFindGrantRelationshipId() throws Exception {
        User user = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        Grant grant = new Grant();

        grant.setAssignee(user);
        grant.setRole(role);

        relationshipManager.add(grant);

        RelationshipQuery<? extends Relationship> query = relationshipManager.createRelationshipQuery(Relationship
                .class);

        query.setParameter(Relationship.ID, grant.getId());

        List<? extends Relationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(grant.getId(), result.get(0).getId());
    }

    @Test
    public void testFindAllRelationshipsForUser() throws Exception {
        User user = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.grantRole(relationshipManager, user, role);
        BasicModel.addToGroup(relationshipManager, user, group);

        RelationshipQuery<Relationship> query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, user);

        List<Relationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, role);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, group);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, user);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }

    @Test
    @Configuration(exclude = LDAPStoreConfigurationTester.class)
    public void testFindByAttributes() throws Exception {
        User someUser = createUser("someUser");
        Role someRole = createRole("someRole");

        Grant grant = new Grant(someUser, someRole);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(grant);

        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute1"), "1");

        List<Grant> result = query.getResultList();

        assertTrue(result.isEmpty());

        grant.setAttribute(new Attribute<String>("attribute1", "1"));
        grant.setAttribute(new Attribute<String[]>("attribute2", new String[]{"1", "2", "3"}));

        relationshipManager.update(grant);

        result = query.getResultList();

        assertEquals(1, result.size());
        assertEquals(grant.getId(), result.get(0).getId());

        grant = result.get(0);

        assertEquals(someUser.getId(), grant.getAssignee().getId());
        assertEquals(someRole.getId(), grant.getRole().getId());

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute1"), "2");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute3"), "2");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(AttributedType.QUERY_ATTRIBUTE.byName("attribute2"), "1", "2", "3");

        result = query.getResultList();

        assertFalse(result.isEmpty());
    }

    @Test
    @Configuration (exclude = LDAPStoreConfigurationTester.class)
    public void testFormalAttributes() throws Exception {
        CustomRelationship relationship = new CustomRelationship();

        User user = createUser("user");

        relationship.setIdentityTypeA(user);

        Role role = createRole("role");

        relationship.setIdentityTypeB(role);

        Group group = createGroup("group");

        relationship.setIdentityTypeC(group);

        relationship.setAttributeA("Value for A");
        relationship.setAttributeB(99l);
        relationship.setAttributeC(true);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(relationship);

        RelationshipQuery<CustomRelationship> query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.ID, relationship.getId());

        List<CustomRelationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
        assertEquals(relationship.getAttributeA(), result.get(0).getAttributeA());
        assertEquals(relationship.getAttributeB(), result.get(0).getAttributeB());
        assertEquals(relationship.isAttributeC(), result.get(0).isAttributeC());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.QUERY_ATTRIBUTE.byName("attributeA"), "Value for A");

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
        assertEquals(relationship.getAttributeA(), result.get(0).getAttributeA());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.QUERY_ATTRIBUTE.byName("attributeA"), "Invalid Value for A");

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.QUERY_ATTRIBUTE.byName("attributeB"), 99l);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
        assertEquals(relationship.getAttributeB(), result.get(0).getAttributeB());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.QUERY_ATTRIBUTE.byName("attributeC"), false);

        result = query.getResultList();

        assertTrue(result.isEmpty());

        query = relationshipManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.QUERY_ATTRIBUTE.byName("attributeC"), true);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
        assertEquals(relationship.isAttributeC(), result.get(0).isAttributeC());
    }
}