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
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for custom {@link Relationship} types.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class CustomRelationshipTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testCreate() throws Exception {
        CustomRelationship relationship = new CustomRelationship();

        IdentityManager identityManager = getIdentityManager();

        SimpleUser user = new SimpleUser("user");

        identityManager.add(user);

        relationship.setIdentityTypeA(user);

        SimpleRole role = new SimpleRole("role");

        identityManager.add(role);

        relationship.setIdentityTypeB(role);

        SimpleGroup group = new SimpleGroup("group");

        identityManager.add(group);

        relationship.setIdentityTypeC(group);

        relationship.setAttributeA("A");
        relationship.setAttributeB("B");
        relationship.setAttributeC("C");

        identityManager.add(relationship);

        RelationshipQuery<CustomRelationship> query = identityManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_A, user);

        List<CustomRelationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
        assertEquals(relationship.getAttributeA(), result.get(0).getAttributeA());
        assertEquals(relationship.getAttributeB(), result.get(0).getAttributeB());
        assertEquals(relationship.getAttributeC(), result.get(0).getAttributeC());

        query = identityManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_B, role);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());

        query = identityManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_C, group);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());

        query = identityManager.createRelationshipQuery(CustomRelationship.class);

        query.setParameter(CustomRelationship.IDENTITY_TYPE_A, user);
        query.setParameter(CustomRelationship.IDENTITY_TYPE_B, role);
        query.setParameter(CustomRelationship.IDENTITY_TYPE_C, group);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(relationship.getId(), result.get(0).getId());
    }

}
