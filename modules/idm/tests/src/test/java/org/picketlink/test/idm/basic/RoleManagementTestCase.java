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

import java.util.Date;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JDBCStoreConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;
import org.picketlink.test.idm.testers.SingleConfigLDAPJPAStoreConfigurationTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Test case for the {@link Role} basic management operations using only the default realm.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include = {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class,
        LDAPStoreConfigurationTester.class, SingleConfigLDAPJPAStoreConfigurationTester.class,
        JDBCStoreConfigurationTester.class})
public class RoleManagementTestCase extends AbstractIdentityTypeTestCase<Role> {

    public RoleManagementTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testCreate() throws Exception {
        Role newRole = createRole("someRole");

        Role storedRole = getRole(newRole.getName());

        assertNotNull(storedRole);
        assertEquals(newRole.getId(), storedRole.getId());
        assertEquals(newRole.getName(), storedRole.getName());
        assertNotNull(storedRole.getPartition());
        assertEquals(Realm.DEFAULT_REALM, storedRole.getPartition().getName());
        assertTrue(storedRole.isEnabled());
        assertNull(storedRole.getExpirationDate());
        assertNotNull(storedRole.getCreatedDate());
        assertTrue(new Date().compareTo(storedRole.getCreatedDate()) >= 0);
    }

    @Test
    public void testRemove() throws Exception {
        Role storedRole = createIdentityType();

        IdentityManager identityManager = getIdentityManager();

        identityManager.remove(storedRole);

        Role removedRole = getRole(storedRole.getName());

        assertNull(removedRole);
        
        User anotherUser = createUser("user");
        Role role = createRole("role");
        Group group = createGroup("group", null);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.grantRole(relationshipManager, anotherUser, role);
        BasicModel.addToGroup(relationshipManager, anotherUser, group);

        RelationshipQuery<?> relationshipQuery = relationshipManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ROLE, role);

        assertFalse(relationshipQuery.getResultList().isEmpty());

        identityManager.remove(role);

        relationshipQuery = relationshipManager.createRelationshipQuery(Grant.class);

        relationshipQuery.setParameter(Grant.ROLE, role);

        assertTrue(relationshipQuery.getResultList().isEmpty());
    }

    @Test
    public void testEqualsMethod() {
        Role instanceA = createRole("roleA");
        Role instanceB = createRole("roleB");
        
        assertFalse(instanceA.equals(instanceB));
        
        assertTrue(instanceA.getName().equals(getRole(instanceA.getName()).getName()));
    }

    @Override
    protected Role createIdentityType() {
        return createRole("Administrator");
    }

    @Override
    protected Role getIdentityType() {
        return getRole("Administrator");
    }

}
