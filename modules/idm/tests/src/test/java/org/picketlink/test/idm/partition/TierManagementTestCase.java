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
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.IdentityLocator;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.Tier;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
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

        getPartitionManager().add(tier, "default");

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

        Role testingRole = IdentityLocator.getRole(applicationTierIdentityManager, "Role");

        if (testingRole != null) {
            applicationTierIdentityManager.remove(testingRole);
        }

        testingRole = new Role("Role");

        applicationTierIdentityManager.add(testingRole);

        testingRole = IdentityLocator.getRole(applicationTierIdentityManager, testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingRole.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();

        testingRole = IdentityLocator.getRole(identityManager, testingRole.getName());

        assertNull(testingRole);
    }

    @Test
    public void testGroupsForTier() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group testingGroup = new Group("testingGroupTier");

        if (IdentityLocator.getGroup(applicationA, testingGroup.getPath()) != null) {
            applicationA.remove(IdentityLocator.getGroup(applicationA, testingGroup.getPath()));
        }

        applicationA.add(testingGroup);

        testingGroup = IdentityLocator.getGroup(applicationA, testingGroup.getName());

        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingGroup.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();
        
        testingGroup = IdentityLocator.getGroup(identityManager, testingGroup.getName());

        assertNull(testingGroup);
    }

    @Test
    public void testCreateSameRoleDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role roleA = new Role("Role");

        if (IdentityLocator.getRole(applicationA, roleA.getName()) != null) {
            applicationA.remove(IdentityLocator.getRole(applicationA, roleA.getName()));
        }

        applicationA.add(roleA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Role(roleA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        roleA = IdentityLocator.getRole(applicationA, roleA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Role roleB = new Role("Role");

        if (IdentityLocator.getRole(applicationB, roleB.getName()) != null) {
            applicationB.remove(IdentityLocator.getRole(applicationB, roleB.getName()));
        }

        applicationB.add(roleB);

        roleA = IdentityLocator.getRole(applicationA, roleA.getName());
        roleB = IdentityLocator.getRole(applicationB, roleB.getName());

        assertFalse(roleA.getId().equals(roleB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (IdentityLocator.getGroup(applicationA, groupA.getPath()) != null) {
            applicationA.remove(IdentityLocator.getGroup(applicationA, groupA.getPath()));
        }

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        groupA = IdentityLocator.getGroup(applicationA, groupA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        if (IdentityLocator.getGroup(applicationB, groupB.getPath()) != null) {
            applicationB.remove(IdentityLocator.getGroup(applicationB, groupB.getPath()));
        }

        applicationB.add(groupB);

        groupA = IdentityLocator.getGroup(applicationA, groupA.getName());
        groupB = IdentityLocator.getGroup(applicationB, groupB.getName());

        assertFalse(groupA.getId().equals(groupB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentRealms() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (IdentityLocator.getGroup(applicationA, groupA.getPath()) != null) {
            applicationA.remove(IdentityLocator.getGroup(applicationA, groupA.getPath()));
        }

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (Exception e) {
        }

        groupA = IdentityLocator.getGroup(applicationA, groupA.getName());

        assertNotNull(groupA);

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        if (IdentityLocator.getGroup(applicationB, groupB.getPath()) != null) {
            applicationB.remove(IdentityLocator.getGroup(applicationB, groupB.getPath()));
        }

        applicationB.add(groupB);

        groupA = IdentityLocator.getGroup(applicationA, groupA.getName());
        groupB = IdentityLocator.getGroup(applicationB, groupB.getName());

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

        assertNull(IdentityLocator.getRole(acmeRealm, roleAName));
        assertNull(IdentityLocator.getRole(acmeRealm, roleBName));
        assertNull(IdentityLocator.getRole(acmeRealm, roleCName));

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        IdentityLocator.grantRole(relationshipManager, john, IdentityLocator.getRole(applicationA, roleAName));
        IdentityLocator.grantRole(relationshipManager, bill, IdentityLocator.getRole(applicationB, roleBName));
        IdentityLocator.grantRole(relationshipManager, mary, IdentityLocator.getRole(applicationC, roleCName));

        assertTrue(IdentityLocator.hasRole(relationshipManager, john, IdentityLocator.getRole(applicationA, roleAName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, john, IdentityLocator.getRole(applicationB, roleBName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, john, IdentityLocator.getRole(applicationC, roleCName)));

        assertTrue(IdentityLocator.hasRole(relationshipManager, bill, IdentityLocator.getRole(applicationB, roleBName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, bill, IdentityLocator.getRole(applicationA, roleAName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, bill, IdentityLocator.getRole(applicationC, roleCName)));

        assertTrue(IdentityLocator.hasRole(relationshipManager, mary, IdentityLocator.getRole(applicationC, roleCName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, mary, IdentityLocator.getRole(applicationA, roleAName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, mary, IdentityLocator.getRole(applicationB, roleBName)));

        IdentityLocator.grantRole(relationshipManager, john, IdentityLocator.getRole(applicationB, roleBName));

        assertTrue(IdentityLocator.hasRole(relationshipManager, john, IdentityLocator.getRole(applicationA, roleAName)));
        assertTrue(IdentityLocator.hasRole(relationshipManager, john, IdentityLocator.getRole(applicationB, roleBName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, john, IdentityLocator.getRole(applicationC, roleCName)));

        applicationA.remove(IdentityLocator.getRole(applicationA, roleAName));

        assertNull(IdentityLocator.getRole(applicationA, roleAName));
        assertTrue(IdentityLocator.hasRole(relationshipManager, bill, IdentityLocator.getRole(applicationB, roleBName)));
        assertTrue(IdentityLocator.hasRole(relationshipManager, mary, IdentityLocator.getRole(applicationC, roleCName)));

        IdentityLocator.revokeRole(relationshipManager, bill, IdentityLocator.getRole(applicationB, roleBName));

        assertFalse(IdentityLocator.hasRole(relationshipManager, bill, IdentityLocator.getRole(applicationB, roleBName)));
        assertTrue(IdentityLocator.hasRole(relationshipManager, mary, IdentityLocator.getRole(applicationC, roleCName)));

        acmeRealm.remove(john);
        acmeRealm.remove(bill);
        acmeRealm.remove(mary);

        assertFalse(IdentityLocator.hasRole(relationshipManager, bill, IdentityLocator.getRole(applicationB, roleBName)));
        assertFalse(IdentityLocator.hasRole(relationshipManager, mary, IdentityLocator.getRole(applicationC, roleCName)));
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

        IdentityLocator.addToGroup(relationshipManager, john, IdentityLocator.getGroup(applicationA, "Group A"));

        IdentityLocator.addToGroup(relationshipManager, bill, IdentityLocator.getGroup(applicationB, "Group B"));

        IdentityLocator.addToGroup(relationshipManager, mary, IdentityLocator.getGroup(applicationC, "Group C"));

        assertTrue(IdentityLocator.isMember(relationshipManager, john, IdentityLocator.getGroup(applicationA, "Group A")));
        assertFalse(IdentityLocator.isMember(relationshipManager, john, IdentityLocator.getGroup(applicationB, "Group B")));
        assertFalse(IdentityLocator.isMember(relationshipManager, john, IdentityLocator.getGroup(applicationC, "Group C")));

        assertTrue(IdentityLocator.isMember(relationshipManager, bill, IdentityLocator.getGroup(applicationB, "Group B")));
        assertFalse(IdentityLocator.isMember(relationshipManager, bill, IdentityLocator.getGroup(applicationA, "Group A")));
        assertFalse(IdentityLocator.isMember(relationshipManager, bill, IdentityLocator.getGroup(applicationC, "Group C")));

        assertTrue(IdentityLocator.isMember(relationshipManager, mary, IdentityLocator.getGroup(applicationC, "Group C")));
        assertFalse(IdentityLocator.isMember(relationshipManager, mary, IdentityLocator.getGroup(applicationA, "Group A")));
        assertFalse(IdentityLocator.isMember(relationshipManager, mary, IdentityLocator.getGroup(applicationB, "Group B")));
    }

    @Test
    public void testGrantSameRoleToTierAndRealm() throws Exception {
        IdentityManager acmeRealm = getIdentityManager();

        Role realmRole = new Role("Role");

        if (IdentityLocator.getRole(acmeRealm, realmRole.getName()) != null) {
            acmeRealm.remove(IdentityLocator.getRole(acmeRealm, realmRole.getName()));
        }

        acmeRealm.add(realmRole);

        IdentityManager application = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role applicationRole = new Role("Role");

        if (IdentityLocator.getRole(application, applicationRole.getName()) != null) {
            application.remove(IdentityLocator.getRole(application, applicationRole.getName()));
        }

        application.add(applicationRole);

        realmRole = IdentityLocator.getRole(acmeRealm, "Role");
        applicationRole = IdentityLocator.getRole(application, "Role");

        assertFalse(realmRole.getId().equals(applicationRole.getId()));

        applicationRole = new Role("Another Role");

        if (IdentityLocator.getRole(application, applicationRole.getName()) != null) {
            application.remove(IdentityLocator.getRole(application, applicationRole.getName()));
        }

        application.add(applicationRole);

        assertNull(IdentityLocator.getRole(acmeRealm, "Another Role"));

        realmRole = new Role("Another Role");

        acmeRealm.add(realmRole);

        assertNotNull(IdentityLocator.getRole(application, "Another Role"));

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
