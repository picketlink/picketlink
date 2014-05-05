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

import org.junit.Test;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAPermissionStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;

import static org.junit.Assert.assertTrue;

/**
 * <p>
 * Test case for {@link org.picketlink.idm.model.annotation.InheritsPrivileges} usages.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Configuration(include= {JPAPermissionStoreConfigurationTester.class, FileStoreConfigurationTester.class, LDAPStoreConfigurationTester.class})
public class InheritsPrivilegeTestCase extends AbstractPartitionManagerTestCase {

    public InheritsPrivilegeTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Test
    public void testInheritsFromRoleAssginedToGroup() throws Exception {
        Role operator = createRole("Operator");
        Group employees = createGroup("Employees");

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(new Grant(employees, operator));

        User john = createUser("john");

        relationshipManager.add(new GroupMembership(john, employees));

        assertTrue(relationshipManager.inheritsPrivileges(john, operator));
    }

    @Test
    public void testInheritsFromRoleAssginedToParentGroup() throws Exception {
        Role operator = createRole("Operator");
        Group itGroup = createGroup("IT");
        Group employees = createGroupWithParent("Employees", itGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(new Grant(itGroup, operator));

        User john = createUser("john");

        relationshipManager.add(new GroupMembership(john, employees));

        assertTrue(relationshipManager.inheritsPrivileges(john, operator));
        assertTrue(relationshipManager.inheritsPrivileges(employees, operator));
    }

    @Test
    public void testInheritsFromRoleAssginedToParentGroupMultipleLevels() throws Exception {
        Role operator = createRole("Operator");
        Group itGroup = createGroup("IT");
        Group pmGroup = createGroupWithParent("Project Management", itGroup);
        Group employees = createGroupWithParent("Employees", pmGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.add(new Grant(itGroup, operator));

        User john = createUser("john");

        relationshipManager.add(new GroupMembership(john, employees));

        assertTrue(relationshipManager.inheritsPrivileges(john, operator));
        assertTrue(relationshipManager.inheritsPrivileges(pmGroup, operator));
    }

}
