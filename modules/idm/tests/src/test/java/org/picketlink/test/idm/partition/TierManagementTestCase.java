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
import org.picketlink.idm.model.sample.SampleModel;
import org.picketlink.idm.model.sample.Tier;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;

import static org.junit.Assert.*;

/**
 * <p>
 * Test case for the {@link Tier} management operations.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
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

        Role testingRole = SampleModel.getRole(applicationTierIdentityManager, "Role");

        if (testingRole != null) {
            applicationTierIdentityManager.remove(testingRole);
        }

        testingRole = new Role("Role");

        applicationTierIdentityManager.add(testingRole);

        testingRole = SampleModel.getRole(applicationTierIdentityManager, testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingRole.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();

        testingRole = SampleModel.getRole(identityManager, testingRole.getName());

        assertNull(testingRole);
    }

    @Test
    public void testGroupsForTier() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group testingGroup = new Group("testingGroupTier");

        if (SampleModel.getGroup(applicationA, testingGroup.getPath()) != null) {
            applicationA.remove(SampleModel.getGroup(applicationA, testingGroup.getPath()));
        }

        applicationA.add(testingGroup);

        testingGroup = SampleModel.getGroup(applicationA, testingGroup.getName());

        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(APPLICATION_A_TIER_NAME, testingGroup.getPartition().getName());

        IdentityManager identityManager = getIdentityManager();
        
        testingGroup = SampleModel.getGroup(identityManager, testingGroup.getName());

        assertNull(testingGroup);
    }

    @Test
    public void testCreateSameRoleDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role roleA = new Role("Role");

        if (SampleModel.getRole(applicationA, roleA.getName()) != null) {
            applicationA.remove(SampleModel.getRole(applicationA, roleA.getName()));
        }

        applicationA.add(roleA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Role(roleA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        roleA = SampleModel.getRole(applicationA, roleA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Role roleB = new Role("Role");

        if (SampleModel.getRole(applicationB, roleB.getName()) != null) {
            applicationB.remove(SampleModel.getRole(applicationB, roleB.getName()));
        }

        applicationB.add(roleB);

        roleA = SampleModel.getRole(applicationA, roleA.getName());
        roleB = SampleModel.getRole(applicationB, roleB.getName());

        assertFalse(roleA.getId().equals(roleB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentTiers() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (SampleModel.getGroup(applicationA, groupA.getPath()) != null) {
            applicationA.remove(SampleModel.getGroup(applicationA, groupA.getPath()));
        }

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (IdentityManagementException e) {
        }

        groupA = SampleModel.getGroup(applicationA, groupA.getName());

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        if (SampleModel.getGroup(applicationB, groupB.getPath()) != null) {
            applicationB.remove(SampleModel.getGroup(applicationB, groupB.getPath()));
        }

        applicationB.add(groupB);

        groupA = SampleModel.getGroup(applicationA, groupA.getName());
        groupB = SampleModel.getGroup(applicationB, groupB.getName());

        assertFalse(groupA.getId().equals(groupB.getId()));
    }

    @Test
    public void testCreateSameGroupDifferentRealms() throws Exception {
        IdentityManager applicationA = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Group groupA = new Group("Group");

        if (SampleModel.getGroup(applicationA, groupA.getPath()) != null) {
            applicationA.remove(SampleModel.getGroup(applicationA, groupA.getPath()));
        }

        applicationA.add(groupA);

        try {
            // we can not add this role with the same name
            applicationA.add(new Group(groupA.getName()));
            fail();
        } catch (Exception e) {
        }

        groupA = SampleModel.getGroup(applicationA, groupA.getName());

        assertNotNull(groupA);

        IdentityManager applicationB = createIdentityManagerForTier(APPLICATION_B_TIER_NAME);

        Group groupB = new Group("Group");

        if (SampleModel.getGroup(applicationB, groupB.getPath()) != null) {
            applicationB.remove(SampleModel.getGroup(applicationB, groupB.getPath()));
        }

        applicationB.add(groupB);

        groupA = SampleModel.getGroup(applicationA, groupA.getName());
        groupB = SampleModel.getGroup(applicationB, groupB.getName());

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

        assertNull(SampleModel.getRole(acmeRealm, roleAName));
        assertNull(SampleModel.getRole(acmeRealm, roleBName));
        assertNull(SampleModel.getRole(acmeRealm, roleCName));

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        SampleModel.grantRole(relationshipManager, john, SampleModel.getRole(applicationA, roleAName));
        SampleModel.grantRole(relationshipManager, bill, SampleModel.getRole(applicationB, roleBName));
        SampleModel.grantRole(relationshipManager, mary, SampleModel.getRole(applicationC, roleCName));

        assertTrue(SampleModel.hasRole(relationshipManager, john, SampleModel.getRole(applicationA, roleAName)));
        assertFalse(SampleModel.hasRole(relationshipManager, john, SampleModel.getRole(applicationB, roleBName)));
        assertFalse(SampleModel.hasRole(relationshipManager, john, SampleModel.getRole(applicationC, roleCName)));

        assertTrue(SampleModel.hasRole(relationshipManager, bill, SampleModel.getRole(applicationB, roleBName)));
        assertFalse(SampleModel.hasRole(relationshipManager, bill, SampleModel.getRole(applicationA, roleAName)));
        assertFalse(SampleModel.hasRole(relationshipManager, bill, SampleModel.getRole(applicationC, roleCName)));

        assertTrue(SampleModel.hasRole(relationshipManager, mary, SampleModel.getRole(applicationC, roleCName)));
        assertFalse(SampleModel.hasRole(relationshipManager, mary, SampleModel.getRole(applicationA, roleAName)));
        assertFalse(SampleModel.hasRole(relationshipManager, mary, SampleModel.getRole(applicationB, roleBName)));

        SampleModel.grantRole(relationshipManager, john, SampleModel.getRole(applicationB, roleBName));

        assertTrue(SampleModel.hasRole(relationshipManager, john, SampleModel.getRole(applicationA, roleAName)));
        assertTrue(SampleModel.hasRole(relationshipManager, john, SampleModel.getRole(applicationB, roleBName)));
        assertFalse(SampleModel.hasRole(relationshipManager, john, SampleModel.getRole(applicationC, roleCName)));

        applicationA.remove(SampleModel.getRole(applicationA, roleAName));

        assertNull(SampleModel.getRole(applicationA, roleAName));
        assertTrue(SampleModel.hasRole(relationshipManager, bill, SampleModel.getRole(applicationB, roleBName)));
        assertTrue(SampleModel.hasRole(relationshipManager, mary, SampleModel.getRole(applicationC, roleCName)));

        SampleModel.revokeRole(relationshipManager, bill, SampleModel.getRole(applicationB, roleBName));

        assertFalse(SampleModel.hasRole(relationshipManager, bill, SampleModel.getRole(applicationB, roleBName)));
        assertTrue(SampleModel.hasRole(relationshipManager, mary, SampleModel.getRole(applicationC, roleCName)));

        acmeRealm.remove(john);
        acmeRealm.remove(bill);
        acmeRealm.remove(mary);

        assertFalse(SampleModel.hasRole(relationshipManager, bill, SampleModel.getRole(applicationB, roleBName)));
        assertFalse(SampleModel.hasRole(relationshipManager, mary, SampleModel.getRole(applicationC, roleCName)));
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

        SampleModel.addToGroup(relationshipManager, john, SampleModel.getGroup(applicationA, "Group A"));

        SampleModel.addToGroup(relationshipManager, bill, SampleModel.getGroup(applicationB, "Group B"));

        SampleModel.addToGroup(relationshipManager, mary, SampleModel.getGroup(applicationC, "Group C"));

        assertTrue(SampleModel.isMember(relationshipManager, john, SampleModel.getGroup(applicationA, "Group A")));
        assertFalse(SampleModel.isMember(relationshipManager, john, SampleModel.getGroup(applicationB, "Group B")));
        assertFalse(SampleModel.isMember(relationshipManager, john, SampleModel.getGroup(applicationC, "Group C")));

        assertTrue(SampleModel.isMember(relationshipManager, bill, SampleModel.getGroup(applicationB, "Group B")));
        assertFalse(SampleModel.isMember(relationshipManager, bill, SampleModel.getGroup(applicationA, "Group A")));
        assertFalse(SampleModel.isMember(relationshipManager, bill, SampleModel.getGroup(applicationC, "Group C")));

        assertTrue(SampleModel.isMember(relationshipManager, mary, SampleModel.getGroup(applicationC, "Group C")));
        assertFalse(SampleModel.isMember(relationshipManager, mary, SampleModel.getGroup(applicationA, "Group A")));
        assertFalse(SampleModel.isMember(relationshipManager, mary, SampleModel.getGroup(applicationB, "Group B")));
    }

    @Test
    public void testGrantSameRoleToTierAndRealm() throws Exception {
        IdentityManager acmeRealm = getIdentityManager();

        Role realmRole = new Role("Role");

        if (SampleModel.getRole(acmeRealm, realmRole.getName()) != null) {
            acmeRealm.remove(SampleModel.getRole(acmeRealm, realmRole.getName()));
        }

        acmeRealm.add(realmRole);

        IdentityManager application = createIdentityManagerForTier(APPLICATION_A_TIER_NAME);

        Role applicationRole = new Role("Role");

        if (SampleModel.getRole(application, applicationRole.getName()) != null) {
            application.remove(SampleModel.getRole(application, applicationRole.getName()));
        }

        application.add(applicationRole);

        realmRole = SampleModel.getRole(acmeRealm, "Role");
        applicationRole = SampleModel.getRole(application, "Role");

        assertFalse(realmRole.getId().equals(applicationRole.getId()));

        applicationRole = new Role("Another Role");

        if (SampleModel.getRole(application, applicationRole.getName()) != null) {
            application.remove(SampleModel.getRole(application, applicationRole.getName()));
        }

        application.add(applicationRole);

        assertNull(SampleModel.getRole(acmeRealm, "Another Role"));

        realmRole = new Role("Another Role");

        acmeRealm.add(realmRole);

        assertNotNull(SampleModel.getRole(application, "Another Role"));

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
