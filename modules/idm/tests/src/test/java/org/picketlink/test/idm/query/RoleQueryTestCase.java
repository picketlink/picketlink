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

package org.picketlink.test.idm.query;

import java.util.List;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * <p>
 * Test case for the Query API when retrieving {@link Role} instances.
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class RoleQueryTestCase extends AbstractIdentityQueryTestCase<Role> {

    public RoleQueryTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected Role createInstance(String name) {
        return new Role(name);
    }

    @Override
    protected Role createIdentityType(String name, Partition partition) {
        if (name == null) {
            name = "someRole";
        }

        return createRole(name, partition);
    }

    @Override
    protected Role getIdentityType() {
        return getIdentityManager().getRole("someRole");
    }

    @After
    public void onFinish() {
        IdentityQuery<Role> query = getIdentityManager().createIdentityQuery(Role.class);

        List<Role> result = query.getResultList();

        for (Role role : result) {
            getIdentityManager().remove(role);
        }
    }

    /**
     * <p>
     * Find an {@link Role} by name.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindByName() throws Exception {
        Role role = createRole("admin");

        IdentityManager identityManager = getIdentityManager();

        IdentityQuery<Role> query = identityManager.<Role> createIdentityQuery(Role.class);

        query.setParameter(Role.NAME, "admin");

        List<Role> result = query.getResultList();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(role.getId(), result.get(0).getId());
    }

    /**
     * <p>
     * Finds all roles for a specific user.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testFindUserRoles() throws Exception {
        Role someRole = createRole("someRole");
        Role someAnotherRole = createRole("someAnotherRole");
        Role someImportantRole = createRole("someImportantRole");

        User user = createUser("someUser");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();
        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, new Object[] { user });

        List<Grant> result = query.getResultList();

        assertFalse(containsGrant(result, someRole));
        assertFalse(containsGrant(result, someAnotherRole));
        assertFalse(containsGrant(result, someImportantRole));

        relationshipManager.grantRole(user, someRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsGrant(result, someRole));
        assertFalse(containsGrant(result, someAnotherRole));
        assertFalse(containsGrant(result, someImportantRole));

        relationshipManager.grantRole(user, someAnotherRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsGrant(result, someRole));
        assertTrue(containsGrant(result, someAnotherRole));
        assertFalse(containsGrant(result, someImportantRole));

        relationshipManager.grantRole(user, someImportantRole);

        query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertTrue(containsGrant(result, someRole));
        assertTrue(containsGrant(result, someAnotherRole));
        assertTrue(containsGrant(result, someImportantRole));

        relationshipManager.revokeRole(user, someRole);

        query.setParameter(Grant.ASSIGNEE, new Object[] { user });

        result = query.getResultList();

        assertFalse(result.isEmpty());
        assertFalse(containsGrant(result, someRole));
        assertTrue(containsGrant(result, someAnotherRole));
        assertTrue(containsGrant(result, someImportantRole));
    }

    @Test
    @Ignore
    public void testFindWithSorting() throws Exception {
        createRole("someRole");
        createRole("someAnotherRole");
        createRole("someImportantRole");

        // Descending sorting by roleName
        IdentityQuery<Role> roleQuery = getIdentityManager().createIdentityQuery(Role.class);

        roleQuery.setSortParameters(Role.NAME);

        roleQuery.setSortAscending(false);

        List<Role> roles = roleQuery.getResultList();

        assertEquals(3, roles.size());
        assertEquals(roles.get(0).getName(), "someRole");
        assertEquals(roles.get(1).getName(), "someImportantRole");
        assertEquals(roles.get(2).getName(), "someAnotherRole");
    }

}
