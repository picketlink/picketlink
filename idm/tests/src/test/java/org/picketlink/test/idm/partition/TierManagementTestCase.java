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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.Tier;
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

    private static final String APPLICATION_TIER_NAME = "Application";

    @Test
    public void testCreateRoles() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();

        Tier applicationTier = new Tier(APPLICATION_TIER_NAME);

        IdentityManager applicationTierIdentityManager = getIdentityManagerFactory().createIdentityManager(applicationTier);

        Role testingRole = createRole("testingRole", applicationTier);

        testingRole = applicationTierIdentityManager.getRole(testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(applicationTier.getId(), testingRole.getPartition().getId());

        testingRole = defaultIdentityManager.getRole(testingRole.getName());

        assertNull(testingRole);
    }

    @Test
    public void testGroupsForTier() throws Exception {
        Tier applicationTier = new Tier(APPLICATION_TIER_NAME);

        IdentityManager applicationTierIdentityManager = getIdentityManagerFactory().createIdentityManager(applicationTier);

        Group testingGroup = createGroup("testingGroupTier", null, applicationTier);

        testingGroup = applicationTierIdentityManager.getGroup(testingGroup.getName());

        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(applicationTier.getId(), testingGroup.getPartition().getId());

        IdentityManager identityManager = getIdentityManager();
        
        testingGroup = identityManager.getGroup(testingGroup.getName());

        assertNull(testingGroup);
    }

    @Test
    public void testRolesForTier() throws Exception {
        Tier applicationTier = new Tier(APPLICATION_TIER_NAME);

        IdentityManager applicationTierIdentityManager = getIdentityManagerFactory().createIdentityManager(applicationTier);

        Role testingRole = createRole("testingRoleTier", applicationTier);

        testingRole = applicationTierIdentityManager.getRole(testingRole.getName());

        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(applicationTier.getId(), testingRole.getPartition().getId());

        IdentityManager identityManager = getIdentityManager();
        
        testingRole = identityManager.getRole(testingRole.getName());

        assertNull(testingRole);
    }
    
    @Test
    public void failAddUserToTier() throws Exception {
        Tier applicationTier = new Tier(APPLICATION_TIER_NAME);
        
        try {
            createUser("testingUserTier", applicationTier);               
        } catch (IdentityManagementException ime) {
            assertTrue(ime.getMessage().contains("PLIDM000067"));  
        } catch (Exception e) {
            fail();
        }
    }

}
