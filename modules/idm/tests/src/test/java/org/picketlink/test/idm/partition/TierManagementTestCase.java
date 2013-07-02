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
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.Tier;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;
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
public class TierManagementTestCase extends AbstractIdentityManagerTestCase {

    private static final String APPLICATION_A_TIER_NAME = "Application A";
    private static final String APPLICATION_B_TIER_NAME = "Application B";
    private static final String APPLICATION_C_TIER_NAME = "Application C";

    @Test
    public void testRolesForTier() throws Exception {
        IdentityManager applicationTierIdentityManager = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role testingRole = new Role("Role");

        applicationTierIdentityManager.add(testingRole);

        testingRole = applicationTierIdentityManager.getRole(testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingRole.getPartition().getId());

        IdentityManager identityManager = getIdentityManager();

        testingRole = identityManager.getRole(testingRole.getName());

        assertNull(testingRole);
    }

    @Test
    public void testGroupsForTier() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group testingGroup = new Group("testingGroupTier");

        applicationA.add(testingGroup);

        testingGroup = applicationA.getGroup(testingGroup.getName());

        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingGroup.getPartition().getId());

        IdentityManager identityManager = getIdentityManager();
        
        testingGroup = identityManager.getGroup(testingGroup.getName());

        assertNull(testingGroup);
    }

    @Test
    public void testCreateSameRoleDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role roleA = new Role("Role");

        applicationA.add(roleA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Role(roleA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        roleA = applicationA.getRole(roleA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Role roleB = new Role("Role");

        applicationB.add(roleB);

        roleA = applicationA.getRole(roleA.getName());
        roleB = applicationB.getRole(roleB.getName());

        assertFalse(roleA.getId().equals(roleB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        groupA = applicationA.getGroup(groupA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        applicationB.add(groupB);

        groupA = applicationA.getGroup(groupA.getName());
        groupB = applicationB.getGroup(groupB.getName());

        assertFalse(groupA.getId().equals(groupB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentRealms() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (Exception e) {
        }

        groupA = applicationA.getGroup(groupA.getName());

        assertNotNull(groupA);

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        applicationB.add(groupB);

        groupA = applicationA.getGroup(groupA.getName());
        groupB = applicationB.getGroup(groupB.getName());

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

        assertNull(acmeRealm.getRole(roleAName));
        assertNull(acmeRealm.getRole(roleBName));
        assertNull(acmeRealm.getRole(roleCName));

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.grantRole(john, applicationA.getRole(roleAName));
        partitionManager.grantRole(bill, applicationB.getRole(roleBName));
        partitionManager.grantRole(mary, applicationC.getRole(roleCName));

        assertTrue(partitionManager.hasRole(john, applicationA.getRole(roleAName)));
        assertFalse(partitionManager.hasRole(john, applicationB.getRole(roleBName)));
        assertFalse(partitionManager.hasRole(john, applicationC.getRole(roleCName)));

        assertTrue(partitionManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertFalse(partitionManager.hasRole(bill, applicationA.getRole(roleAName)));
        assertFalse(partitionManager.hasRole(bill, applicationC.getRole(roleCName)));

        assertTrue(partitionManager.hasRole(mary, applicationC.getRole(roleCName)));
        assertFalse(partitionManager.hasRole(mary, applicationA.getRole(roleAName)));
        assertFalse(partitionManager.hasRole(mary, applicationB.getRole(roleBName)));

        partitionManager.grantRole(john, applicationB.getRole(roleBName));

        assertTrue(partitionManager.hasRole(john, applicationA.getRole(roleAName)));
        assertTrue(partitionManager.hasRole(john, applicationB.getRole(roleBName)));
        assertFalse(partitionManager.hasRole(john, applicationC.getRole(roleCName)));

        applicationA.remove(applicationA.getRole(roleAName));

        assertNull(applicationA.getRole(roleAName));
        assertTrue(partitionManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertTrue(partitionManager.hasRole(mary, applicationC.getRole(roleCName)));

        partitionManager.revokeRole(bill, applicationB.getRole(roleBName));

        assertFalse(partitionManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertTrue(partitionManager.hasRole(mary, applicationC.getRole(roleCName)));

        acmeRealm.remove(john);
        acmeRealm.remove(bill);
        acmeRealm.remove(mary);

        assertFalse(partitionManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertFalse(partitionManager.hasRole(mary, applicationC.getRole(roleCName)));
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

        PartitionManager partitionManager = getPartitionManager();

        partitionManager.addToGroup(john, applicationA.getGroup("Group A"));

        partitionManager.addToGroup(bill, applicationB.getGroup("Group B"));

        partitionManager.addToGroup(mary, applicationC.getGroup("Group C"));

        assertTrue(partitionManager.isMember(john, applicationA.getGroup("Group A")));
        assertFalse(partitionManager.isMember(john, applicationB.getGroup("Group B")));
        assertFalse(partitionManager.isMember(john, applicationC.getGroup("Group C")));

        assertTrue(partitionManager.isMember(bill, applicationB.getGroup("Group B")));
        assertFalse(partitionManager.isMember(bill, applicationA.getGroup("Group A")));
        assertFalse(partitionManager.isMember(bill, applicationC.getGroup("Group C")));

        assertTrue(partitionManager.isMember(mary, applicationC.getGroup("Group C")));
        assertFalse(partitionManager.isMember(mary, applicationA.getGroup("Group A")));
        assertFalse(partitionManager.isMember(mary, applicationB.getGroup("Group B")));
    }

    @Test
    public void testGrantSameRoleToTierAndRealm() throws Exception {
        IdentityManager acmeRealm = getIdentityManager();

        Role realmRole = new Role("Role");

        acmeRealm.add(realmRole);

        IdentityManager application = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role applicationRole = new Role("Role");

        application.add(applicationRole);

        realmRole = acmeRealm.getRole("Role");
        applicationRole = application.getRole("Role");

        assertFalse(realmRole.getId().equals(applicationRole.getId()));

        applicationRole = new Role("Another Role");

        application.add(applicationRole);

        assertNull(acmeRealm.getRole("Another Role"));

        realmRole = new Role("Another Role");

        acmeRealm.add(realmRole);

        assertNotNull(application.getRole("Another Role"));

        assertFalse(realmRole.getId().equals(applicationRole.getId()));
    }
    
    @Test
    public void failAddUserToTier() throws Exception {
        Tier applicationTier = new Tier(APPLICATION_A_TIER_NAME);
        
        try {
            createUser("testingUserTier", applicationTier);               
        } catch (IdentityManagementException ime) {
            assertTrue(ime.getMessage().contains("PLIDM000067"));  
        } catch (Exception e) {
            fail();
        }
    }

    private IdentityManager createIdentityManagerForTier(String tierName) {
        return getPartitionManager().createIdentityManager(getPartitionManager().getPartition(Tier.class, tierName));
    }

}
