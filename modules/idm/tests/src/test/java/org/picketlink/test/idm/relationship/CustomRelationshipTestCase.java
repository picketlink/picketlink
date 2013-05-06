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

        User user = createUser("user");

        relationship.setIdentityTypeA(user);

        Role role = createRole("role");

        relationship.setIdentityTypeB(role);

        Group group = createGroup("group");

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
