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

import java.util.List;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.SampleModel;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.IgnoreTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Pedro Silva
 *
 */
@IgnoreTester(LDAPStoreConfigurationTester.class)
public class RelationshipQueryTestCase extends AbstractPartitionManagerTestCase {

    public RelationshipQueryTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testFindAllRelationshipsForUser() throws Exception {
        User user = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        IdentityManager identityManager = getIdentityManager();

        SampleModel.grantRole(relationshipManager, user, role);
        SampleModel.grantGroupRole(relationshipManager, user, role, group);
        SampleModel.addToGroup(relationshipManager, user, group);

        RelationshipQuery<Relationship> query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, user);

        List<Relationship> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(3, result.size());

        query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, role);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, group);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        query = relationshipManager.createRelationshipQuery(Relationship.class);

        query.setParameter(Relationship.IDENTITY, user);

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
    }
    
}