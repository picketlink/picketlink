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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>
 * Test case for the {@link Tier} management operations.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
public class TierManagementTestCase extends AbstractIdentityManagerTestCase {

    private static final String SERVICES_TIER_NAME = "Services";
    private static final String APPLICATION_TIER_NAME = "Application";
    private static final String TESTING_TIER_NAME = "Testing Tier";

    @Test
    public void testCreate() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier testingTier = createTestingTier();

        testingTier = identityManager.getTier(testingTier.getName());

        assertNotNull(testingTier);
        assertEquals(TESTING_TIER_NAME, testingTier.getName());
        assertEquals(Tier.KEY_PREFIX + TESTING_TIER_NAME, testingTier.getKey());
    }

    @Test
    public void testCreateWithParent() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier applicationTier = createApplicationTier();

        applicationTier = identityManager.getTier(applicationTier.getName());

        Tier serviceTier = createServicesTier(applicationTier);

        serviceTier = identityManager.getTier(serviceTier.getName());

        assertNotNull(serviceTier);
        assertEquals(SERVICES_TIER_NAME, serviceTier.getName());
        assertNotNull(serviceTier.getParent());
        assertEquals(applicationTier.getName(), serviceTier.getParent().getName());
    }

    @Test
    public void testRemoveTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier testingTier = createTestingTier();

        assertNotNull(testingTier);

        identityManager.removeTier(testingTier);

        testingTier = identityManager.getTier(testingTier.getName());

        assertNull(testingTier);
    }

    @Test(expected = IdentityManagementException.class)
    public void testRemoveWithRoles() throws Exception {
        Tier applicationTier = createApplicationTier();

        IdentityManager defaultIdentityManager = getIdentityManager();

        IdentityManager applicationTierIdentityManager = defaultIdentityManager.forTier(applicationTier);

        Role testingRole = new SimpleRole("testingRole");

        // should throw an exception because the current tier has IdentityTypes associated.
        applicationTierIdentityManager.add(testingRole);

        defaultIdentityManager.removeTier(applicationTier);
    }

    @Test(expected = IdentityManagementException.class)
    public void testRemoveTierGroups() throws Exception {
        Tier applicationTier = createApplicationTier();

        IdentityManager defaultIdentityManager = getIdentityManager();

        IdentityManager applicationTierIdentityManager = defaultIdentityManager.forTier(applicationTier);

        Group testingGroup = new SimpleGroup("testingGroup");

        // should throw an exception because the current tier has IdentityTypes associated.
        applicationTierIdentityManager.add(testingGroup);

        defaultIdentityManager.removeTier(applicationTier);
    }

    @Test
    public void testRolesForTierWithParent() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();

        Tier parentTier = createApplicationTier();

        Role testingRole = createRole("testingRole", parentTier);
        
        testingRole = defaultIdentityManager.forTier(parentTier).getRole("testingRole");
        
        assertNotNull(testingRole);
        
        // create a tier with no parent
        Tier servicesTier = createServicesTier(null);
        
        // the identitytype is not visible to the servicesTier 
        testingRole = defaultIdentityManager.forTier(servicesTier).getRole("testingRole");
        
        assertNull(testingRole);
        
        // create a tier as a child of the parent tier
        servicesTier = createServicesTier(parentTier);
        
        // the identitytype is now visible to the servicesTier
        testingRole = defaultIdentityManager.forTier(servicesTier).getRole("testingRole");
        
        assertNotNull(testingRole);
    }
    
    @Test
    public void testCreateRoles() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();

        Tier applicationTier = createApplicationTier();

        IdentityManager applicationTierIdentityManager = defaultIdentityManager.forTier(applicationTier);

        Role testingRole = createRole("testingRole", applicationTier);

        testingRole = applicationTierIdentityManager.getRole(testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(applicationTier.getId(), testingRole.getPartition().getId());

        testingRole = defaultIdentityManager.getRole(testingRole.getName());

        assertNull(testingRole);
    }

    @Test
    public void testGrantUserRoles() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();

        Tier applicationTier = createApplicationTier();

        IdentityManager applicationTierIdentityManager = defaultIdentityManager.forTier(applicationTier);

        Role adminRole = createRole("Administrator", applicationTier);

        adminRole = applicationTierIdentityManager.getRole(adminRole.getName());

        assertNotNull(adminRole);

        Realm testingRealm = createRealm();

        IdentityManager testingRealmIdentityManager = applicationTierIdentityManager.forRealm(testingRealm);

        User someUser = createUser("someUser", testingRealm);

        testingRealmIdentityManager.grantRole(someUser, adminRole);

        assertTrue(testingRealmIdentityManager.hasRole(someUser, adminRole));

        // application tier is bound to the default realm. the user was created only for the testing realm.
        assertFalse(applicationTierIdentityManager.hasRole(someUser, adminRole));
    }

    @Test
    public void testGroupsForTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Tier applicationTier = createApplicationTier();

        IdentityManager applicationTierIdentityManager = identityManager.forTier(applicationTier);

        Group testingGroup = createGroup("testingGroupTier", null, applicationTier);

        testingGroup = applicationTierIdentityManager.getGroup(testingGroup.getName());

        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(applicationTier.getId(), testingGroup.getPartition().getId());

        testingGroup = identityManager.getGroup(testingGroup.getName());

        assertNull(testingGroup);
    }
    
    @Test
    public void testGroupsForTierWithParent() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();

        Tier parentTier = createApplicationTier();

        Group testingGroup = createGroup("testingGroupParentTier", null, parentTier);
        
        testingGroup = defaultIdentityManager.forTier(parentTier).getGroup("testingGroupParentTier");
        
        assertNotNull(testingGroup);
        
        // create a tier with no parent
        Tier servicesTier = createServicesTier(null);
        
        // the identitytype is not visible to the servicesTier 
        testingGroup = defaultIdentityManager.forTier(servicesTier).getGroup("testingGroupParentTier");
        
        assertNull(testingGroup);
        
        // create a tier as a child of the parent tier
        servicesTier = createServicesTier(parentTier);
        
        // the identitytype is now visible to the servicesTier
        testingGroup = defaultIdentityManager.forTier(servicesTier).getGroup("testingGroupParentTier");
        
        assertNotNull(testingGroup);
    }

    private Tier createApplicationTier() {
        IdentityManager identityManager = getIdentityManager();

        Tier tier = identityManager.getTier(APPLICATION_TIER_NAME);

        if (tier == null) {
            tier = new Tier(APPLICATION_TIER_NAME);
            identityManager.createTier(tier);
        }

        return tier;
    }
    
    private Tier createTestingTier() {
        IdentityManager identityManager = getIdentityManager();

        Tier tier = identityManager.getTier(TESTING_TIER_NAME);

        if (tier == null) {
            tier = new Tier(TESTING_TIER_NAME);
            identityManager.createTier(tier);
        }

        return tier;
    }
    
    private Tier createServicesTier(Tier parentTier) {
        IdentityManager identityManager = getIdentityManager();

        Tier serviceTier = identityManager.getTier(SERVICES_TIER_NAME);

        if (serviceTier != null) {
            identityManager.removeTier(serviceTier);
        }

        if (parentTier != null) {
            serviceTier = new Tier(SERVICES_TIER_NAME, parentTier);
        } else {
            serviceTier = new Tier(SERVICES_TIER_NAME);
        }

        identityManager.createTier(serviceTier);

        return serviceTier;
    }
    
    private Realm createRealm() {
        IdentityManager identityManager = getIdentityManager();

        Realm realm = identityManager.getRealm("Testing");

        if (realm == null) {
            realm = new Realm("Testing");
            identityManager.createRealm(realm);
        }

        return realm;
    }
}
