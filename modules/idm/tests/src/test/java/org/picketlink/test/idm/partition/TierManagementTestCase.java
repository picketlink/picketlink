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
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.Tier;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.IdentityConfigurationTester;
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

        Role testingRole = new Role("Role");

        if (applicationTierIdentityManager.getRole(testingRole.getName()) != null) {
            applicationTierIdentityManager.remove(applicationTierIdentityManager.getRole(testingRole.getName()));
        }

        applicationTierIdentityManager.add(testingRole);

        testingRole = applicationTierIdentityManager.getRole(testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingRole.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();

        testingRole = identityManager.getRole(testingRole.getName());

        assertNull(testingRole);
    }

    @Test
    public void testGroupsForTier() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group testingGroup = new Group("testingGroupTier");

        if (applicationA.getGroup(testingGroup.getPath()) != null) {
            applicationA.remove(applicationA.getGroup(testingGroup.getPath()));
        }

        applicationA.add(testingGroup);

        testingGroup = applicationA.getGroup(testingGroup.getName());

        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingGroup.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();
        
        testingGroup = identityManager.getGroup(testingGroup.getName());

        assertNull(testingGroup);
    }

    @Test
    public void testCreateSameRoleDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role roleA = new Role("Role");

        if (applicationA.getRole(roleA.getName()) != null) {
            applicationA.remove(applicationA.getRole(roleA.getName()));
        }

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

        if (applicationB.getRole(roleB.getName()) != null) {
            applicationB.remove(applicationB.getRole(roleB.getName()));
        }

        applicationB.add(roleB);

        roleA = applicationA.getRole(roleA.getName());
        roleB = applicationB.getRole(roleB.getName());

        assertFalse(roleA.getId().equals(roleB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (applicationA.getGroup(groupA.getPath()) != null) {
            applicationA.remove(applicationA.getGroup(groupA.getPath()));
        }

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

        if (applicationB.getGroup(groupB.getPath()) != null) {
            applicationB.remove(applicationB.getGroup(groupB.getPath()));
        }

        applicationB.add(groupB);

        groupA = applicationA.getGroup(groupA.getName());
        groupB = applicationB.getGroup(groupB.getName());

        assertFalse(groupA.getId().equals(groupB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentRealms() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (applicationA.getGroup(groupA.getPath()) != null) {
            applicationA.remove(applicationA.getGroup(groupA.getPath()));
        }

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

        if (applicationB.getGroup(groupB.getPath()) != null) {
            applicationB.remove(applicationB.getGroup(groupB.getPath()));
        }

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

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        relationshipManager.grantRole(john, applicationA.getRole(roleAName));
        relationshipManager.grantRole(bill, applicationB.getRole(roleBName));
        relationshipManager.grantRole(mary, applicationC.getRole(roleCName));

        assertTrue(relationshipManager.hasRole(john, applicationA.getRole(roleAName)));
        assertFalse(relationshipManager.hasRole(john, applicationB.getRole(roleBName)));
        assertFalse(relationshipManager.hasRole(john, applicationC.getRole(roleCName)));

        assertTrue(relationshipManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertFalse(relationshipManager.hasRole(bill, applicationA.getRole(roleAName)));
        assertFalse(relationshipManager.hasRole(bill, applicationC.getRole(roleCName)));

        assertTrue(relationshipManager.hasRole(mary, applicationC.getRole(roleCName)));
        assertFalse(relationshipManager.hasRole(mary, applicationA.getRole(roleAName)));
        assertFalse(relationshipManager.hasRole(mary, applicationB.getRole(roleBName)));

        relationshipManager.grantRole(john, applicationB.getRole(roleBName));

        assertTrue(relationshipManager.hasRole(john, applicationA.getRole(roleAName)));
        assertTrue(relationshipManager.hasRole(john, applicationB.getRole(roleBName)));
        assertFalse(relationshipManager.hasRole(john, applicationC.getRole(roleCName)));

        applicationA.remove(applicationA.getRole(roleAName));

        assertNull(applicationA.getRole(roleAName));
        assertTrue(relationshipManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertTrue(relationshipManager.hasRole(mary, applicationC.getRole(roleCName)));

        relationshipManager.revokeRole(bill, applicationB.getRole(roleBName));

        assertFalse(relationshipManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertTrue(relationshipManager.hasRole(mary, applicationC.getRole(roleCName)));

        acmeRealm.remove(john);
        acmeRealm.remove(bill);
        acmeRealm.remove(mary);

        assertFalse(relationshipManager.hasRole(bill, applicationB.getRole(roleBName)));
        assertFalse(relationshipManager.hasRole(mary, applicationC.getRole(roleCName)));
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

        relationshipManager.addToGroup(john, applicationA.getGroup("Group A"));

        relationshipManager.addToGroup(bill, applicationB.getGroup("Group B"));

        relationshipManager.addToGroup(mary, applicationC.getGroup("Group C"));

        assertTrue(relationshipManager.isMember(john, applicationA.getGroup("Group A")));
        assertFalse(relationshipManager.isMember(john, applicationB.getGroup("Group B")));
        assertFalse(relationshipManager.isMember(john, applicationC.getGroup("Group C")));

        assertTrue(relationshipManager.isMember(bill, applicationB.getGroup("Group B")));
        assertFalse(relationshipManager.isMember(bill, applicationA.getGroup("Group A")));
        assertFalse(relationshipManager.isMember(bill, applicationC.getGroup("Group C")));

        assertTrue(relationshipManager.isMember(mary, applicationC.getGroup("Group C")));
        assertFalse(relationshipManager.isMember(mary, applicationA.getGroup("Group A")));
        assertFalse(relationshipManager.isMember(mary, applicationB.getGroup("Group B")));
    }

    @Test
    public void testGrantSameRoleToTierAndRealm() throws Exception {
        IdentityManager acmeRealm = getIdentityManager();

        Role realmRole = new Role("Role");

        if (acmeRealm.getRole(realmRole.getName()) != null) {
            acmeRealm.remove(acmeRealm.getRole(realmRole.getName()));
        }

        acmeRealm.add(realmRole);

        IdentityManager application = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role applicationRole = new Role("Role");

        if (application.getRole(applicationRole.getName()) != null) {
            application.remove(application.getRole(applicationRole.getName()));
        }

        application.add(applicationRole);

        realmRole = acmeRealm.getRole("Role");
        applicationRole = application.getRole("Role");

        assertFalse(realmRole.getId().equals(applicationRole.getId()));

        applicationRole = new Role("Another Role");

        if (application.getRole(applicationRole.getName()) != null) {
            application.remove(application.getRole(applicationRole.getName()));
        }

        application.add(applicationRole);

        assertNull(acmeRealm.getRole("Another Role"));

        realmRole = new Role("Another Role");

        acmeRealm.add(realmRole);

        assertNotNull(application.getRole("Another Role"));

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
