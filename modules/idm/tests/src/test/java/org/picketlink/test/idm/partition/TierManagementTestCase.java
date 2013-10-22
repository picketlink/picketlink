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

package org.picketlink.test.idm.partition;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.Tier;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>
 * Test case for the {@link Tier} management operations.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
public class TierManagementTestCase extends AbstractPartitionTestCase<Tier> {

    private static final String DEFAULT_TIER_NAME = "Default Tier";
    private static final String APPLICATION_A_TIER_NAME = "Application A";
    private static final String APPLICATION_B_TIER_NAME = "Application B";
    private static final String APPLICATION_C_TIER_NAME = "Application C";

    public TierManagementTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected Tier createPartition() {
        Tier tier = new Tier(DEFAULT_TIER_NAME);

        if (getPartitionManager().getPartition(tier.getClass(), tier.getName()) != null) {
            getPartitionManager().remove(tier);
        }

        getPartitionManager().add(tier);

        return tier;
    }

    @Override
    protected Tier getPartition() {
        return getPartitionManager().getPartition(Tier.class, DEFAULT_TIER_NAME);
    }

    @Test
    public void failAddUserToTier() throws Exception {
        try {
            createUser("someUser", createPartition());
        } catch (IdentityManagementException ime) {
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testRolesForTier() throws Exception {
        IdentityManager applicationTierIdentityManager = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role testingRole = BasicModel.getRole(applicationTierIdentityManager, "Role");

        if (testingRole != null) {
            applicationTierIdentityManager.remove(testingRole);
        }

        testingRole = new Role("Role");

        applicationTierIdentityManager.add(testingRole);

        testingRole = BasicModel.getRole(applicationTierIdentityManager, testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingRole.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();

        testingRole = BasicModel.getRole(identityManager, testingRole.getName());

        assertNull(testingRole);
    }

    @Test
    @Configuration (exclude = LDAPUserGroupJPARoleConfigurationTester.class)
    public void testGroupsForTier() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group testingGroup = new Group("testingGroupTier");

        if (BasicModel.getGroup(applicationA, testingGroup.getPath()) != null) {
            applicationA.remove(BasicModel.getGroup(applicationA, testingGroup.getPath()));
        }

        applicationA.add(testingGroup);

        testingGroup = BasicModel.getGroup(applicationA, testingGroup.getName());

        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingGroup.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();
        
        testingGroup = BasicModel.getGroup(identityManager, testingGroup.getName());

        assertNull(testingGroup);
    }

    @Test
    public void testCreateSameRoleDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role roleA = new Role("Role");

        if (BasicModel.getRole(applicationA, roleA.getName()) != null) {
            applicationA.remove(BasicModel.getRole(applicationA, roleA.getName()));
        }

        applicationA.add(roleA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Role(roleA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        roleA = BasicModel.getRole(applicationA, roleA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Role roleB = new Role("Role");

        if (BasicModel.getRole(applicationB, roleB.getName()) != null) {
            applicationB.remove(BasicModel.getRole(applicationB, roleB.getName()));
        }

        applicationB.add(roleB);

        roleA = BasicModel.getRole(applicationA, roleA.getName());
        roleB = BasicModel.getRole(applicationB, roleB.getName());

        assertFalse(roleA.getId().equals(roleB.getId()));
    }

    @Test
    @Configuration (exclude = LDAPUserGroupJPARoleConfigurationTester.class)
    public void testCreateSameGroupDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (BasicModel.getGroup(applicationA, groupA.getPath()) != null) {
            applicationA.remove(BasicModel.getGroup(applicationA, groupA.getPath()));
        }

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        groupA = BasicModel.getGroup(applicationA, groupA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        if (BasicModel.getGroup(applicationB, groupB.getPath()) != null) {
            applicationB.remove(BasicModel.getGroup(applicationB, groupB.getPath()));
        }

        applicationB.add(groupB);

        groupA = BasicModel.getGroup(applicationA, groupA.getName());
        groupB = BasicModel.getGroup(applicationB, groupB.getName());

        assertFalse(groupA.getId().equals(groupB.getId()));
    }

    @Test
    @Configuration (exclude = LDAPUserGroupJPARoleConfigurationTester.class)
    public void testCreateSameGroupDifferentRealms() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (BasicModel.getGroup(applicationA, groupA.getPath()) != null) {
            applicationA.remove(BasicModel.getGroup(applicationA, groupA.getPath()));
        }

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (Exception e) {
        }

        groupA = BasicModel.getGroup(applicationA, groupA.getName());

        assertNotNull(groupA);

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        if (BasicModel.getGroup(applicationB, groupB.getPath()) != null) {
            applicationB.remove(BasicModel.getGroup(applicationB, groupB.getPath()));
        }

        applicationB.add(groupB);

        groupA = BasicModel.getGroup(applicationA, groupA.getName());
        groupB = BasicModel.getGroup(applicationB, groupB.getName());

        assertFalse(groupA.getId().equals(groupB.getId()));
    }

    @Test
    public void testGrantUserRoles() throws Exception {
        IdentityManager acmeRealm = getIdentityManager();

        User john = new User("John");
        User bill = new User("Bill");
        User mary = new User("Mary");

        acmeRealm.add(john);
        acmeRealm.add(bill);
        acmeRealm.add(mary);

        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        String roleAName = "Role A";
        String roleCName = "Role C";
        String roleBName = "Role B";

        applicationA.add(new Role(roleAName));

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        applicationB.add(new Role(roleBName));

        IdentityManager applicationC = createIdentityManagerForTier(APPLICATION_C_TIER_NAME);

        applicationC.add(new Role(roleCName));

        assertNull(BasicModel.getRole(acmeRealm, roleAName));
        assertNull(BasicModel.getRole(acmeRealm, roleBName));
        assertNull(BasicModel.getRole(acmeRealm, roleCName));

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.grantRole(relationshipManager, john, BasicModel.getRole(applicationA, roleAName));
        BasicModel.grantRole(relationshipManager, bill, BasicModel.getRole(applicationB, roleBName));
        BasicModel.grantRole(relationshipManager, mary, BasicModel.getRole(applicationC, roleCName));

        assertTrue(BasicModel.hasRole(relationshipManager, john, BasicModel.getRole(applicationA, roleAName)));
        assertFalse(BasicModel.hasRole(relationshipManager, john, BasicModel.getRole(applicationB, roleBName)));
        assertFalse(BasicModel.hasRole(relationshipManager, john, BasicModel.getRole(applicationC, roleCName)));

        assertTrue(BasicModel.hasRole(relationshipManager, bill, BasicModel.getRole(applicationB, roleBName)));
        assertFalse(BasicModel.hasRole(relationshipManager, bill, BasicModel.getRole(applicationA, roleAName)));
        assertFalse(BasicModel.hasRole(relationshipManager, bill, BasicModel.getRole(applicationC, roleCName)));

        assertTrue(BasicModel.hasRole(relationshipManager, mary, BasicModel.getRole(applicationC, roleCName)));
        assertFalse(BasicModel.hasRole(relationshipManager, mary, BasicModel.getRole(applicationA, roleAName)));
        assertFalse(BasicModel.hasRole(relationshipManager, mary, BasicModel.getRole(applicationB, roleBName)));

        BasicModel.grantRole(relationshipManager, john, BasicModel.getRole(applicationB, roleBName));

        assertTrue(BasicModel.hasRole(relationshipManager, john, BasicModel.getRole(applicationA, roleAName)));
        assertTrue(BasicModel.hasRole(relationshipManager, john, BasicModel.getRole(applicationB, roleBName)));
        assertFalse(BasicModel.hasRole(relationshipManager, john, BasicModel.getRole(applicationC, roleCName)));

        applicationA.remove(BasicModel.getRole(applicationA, roleAName));

        assertNull(BasicModel.getRole(applicationA, roleAName));
        assertTrue(BasicModel.hasRole(relationshipManager, bill, BasicModel.getRole(applicationB, roleBName)));
        assertTrue(BasicModel.hasRole(relationshipManager, mary, BasicModel.getRole(applicationC, roleCName)));

        BasicModel.revokeRole(relationshipManager, bill, BasicModel.getRole(applicationB, roleBName));

        assertFalse(BasicModel.hasRole(relationshipManager, bill, BasicModel.getRole(applicationB, roleBName)));
        assertTrue(BasicModel.hasRole(relationshipManager, mary, BasicModel.getRole(applicationC, roleCName)));

        acmeRealm.remove(john);
        acmeRealm.remove(bill);
        acmeRealm.remove(mary);

        assertFalse(BasicModel.hasRole(relationshipManager, bill, BasicModel.getRole(applicationB, roleBName)));
        assertFalse(BasicModel.hasRole(relationshipManager, mary, BasicModel.getRole(applicationC, roleCName)));
    }

    @Test
    public void testGrantUserGroups() throws Exception {
        IdentityManager acmeRealm = getIdentityManager();

        User john = new User("John");
        User bill = new User("Bill");
        User mary = new User("Mary");

        acmeRealm.add(john);
        acmeRealm.add(bill);
        acmeRealm.add(mary);

        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        applicationA.add(new Group("Group A"));

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        applicationB.add(new Group("Group B"));

        IdentityManager applicationC = createIdentityManagerForTier(APPLICATION_C_TIER_NAME);

        applicationC.add(new Group("Group C"));

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.addToGroup(relationshipManager, john, BasicModel.getGroup(applicationA, "Group A"));

        BasicModel.addToGroup(relationshipManager, bill, BasicModel.getGroup(applicationB, "Group B"));

        BasicModel.addToGroup(relationshipManager, mary, BasicModel.getGroup(applicationC, "Group C"));

        assertTrue(BasicModel.isMember(relationshipManager, john, BasicModel.getGroup(applicationA, "Group A")));
        assertFalse(BasicModel.isMember(relationshipManager, john, BasicModel.getGroup(applicationB, "Group B")));
        assertFalse(BasicModel.isMember(relationshipManager, john, BasicModel.getGroup(applicationC, "Group C")));

        assertTrue(BasicModel.isMember(relationshipManager, bill, BasicModel.getGroup(applicationB, "Group B")));
        assertFalse(BasicModel.isMember(relationshipManager, bill, BasicModel.getGroup(applicationA, "Group A")));
        assertFalse(BasicModel.isMember(relationshipManager, bill, BasicModel.getGroup(applicationC, "Group C")));

        assertTrue(BasicModel.isMember(relationshipManager, mary, BasicModel.getGroup(applicationC, "Group C")));
        assertFalse(BasicModel.isMember(relationshipManager, mary, BasicModel.getGroup(applicationA, "Group A")));
        assertFalse(BasicModel.isMember(relationshipManager, mary, BasicModel.getGroup(applicationB, "Group B")));
    }

    @Test
    public void testGrantSameRoleToTierAndRealm() throws Exception {
        IdentityManager acmeRealm = getIdentityManager();

        Role realmRole = new Role("Role");

        if (BasicModel.getRole(acmeRealm, realmRole.getName()) != null) {
            acmeRealm.remove(BasicModel.getRole(acmeRealm, realmRole.getName()));
        }

        acmeRealm.add(realmRole);

        IdentityManager application = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role applicationRole = new Role("Role");

        if (BasicModel.getRole(application, applicationRole.getName()) != null) {
            application.remove(BasicModel.getRole(application, applicationRole.getName()));
        }

        application.add(applicationRole);

        realmRole = BasicModel.getRole(acmeRealm, "Role");
        applicationRole = BasicModel.getRole(application, "Role");

        assertFalse(realmRole.getId().equals(applicationRole.getId()));

        applicationRole = new Role("Another Role");

        if (BasicModel.getRole(application, applicationRole.getName()) != null) {
            application.remove(BasicModel.getRole(application, applicationRole.getName()));
        }

        application.add(applicationRole);

        assertNull(BasicModel.getRole(acmeRealm, "Another Role"));

        realmRole = new Role("Another Role");

        acmeRealm.add(realmRole);

        assertNotNull(BasicModel.getRole(application, "Another Role"));

        assertFalse(realmRole.getId().equals(applicationRole.getId()));
    }
    
    private IdentityManager createIdentityManagerForTier(String tierName) {
        Tier partition = getPartitionManager().getPartition(Tier.class, tierName);

        if (partition == null) {
            partition = new Tier(tierName);
            getPartitionManager().add(partition);
        }

        return getPartitionManager().createIdentityManager(partition);
    }

}
