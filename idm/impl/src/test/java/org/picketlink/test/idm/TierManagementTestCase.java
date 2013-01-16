/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.test.idm;

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.model.User;

/**
 * @author Pedro Silva
 *
 */
public class TierManagementTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testCreateTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Tier tier = createApplicationTier();
        
        tier = identityManager.getTier(tier.getName());
        
        Assert.assertNotNull(tier);
        Assert.assertEquals("Application", tier.getName());
        Assert.assertEquals(tier.KEY_PREFIX + "Application", tier.getKey());
    }

    private Tier createApplicationTier() {
        IdentityManager identityManager = getIdentityManager();
        
        Tier tier = identityManager.getTier("Application");
        
        if (tier != null) {
            identityManager.removeTier(tier);
        }
        
        tier = new Tier("Application");
        
        identityManager.createTier(tier);
        
        return tier;
    }
    
    @Test
    public void testRemoveTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Tier applicationTier = createApplicationTier();
        
        Assert.assertNotNull(applicationTier);
        
        identityManager.removeTier(applicationTier);
        
        applicationTier = identityManager.getTier(applicationTier.getName());
        
        Assert.assertNull(applicationTier);
    }
    
    @Test
    public void testRolesForTier() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Tier applicationTier = createApplicationTier();
        
        IdentityManager applicationTierIdentityManager = defaultIdentityManager.forTier(applicationTier);
        
        Role testingRole = new SimpleRole("testingRole");
        
        applicationTierIdentityManager.add(testingRole);
        
        testingRole = applicationTierIdentityManager.getRole(testingRole.getName());
        
        Assert.assertNotNull(testingRole);
        Assert.assertNotNull(testingRole);
        Assert.assertNotNull(testingRole.getPartition());
        Assert.assertEquals(applicationTier.getId(), testingRole.getPartition().getId());
        
        testingRole = defaultIdentityManager.getRole(testingRole.getName());
        
        Assert.assertNull(testingRole);
    }
    
    @Test
    public void testUserRolesForTier() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Tier applicationTier = createApplicationTier();
        
        IdentityManager applicationTierIdentityManager = defaultIdentityManager.forTier(applicationTier);
        
        Role adminRole = new SimpleRole("Administrator");
        
        applicationTierIdentityManager.add(adminRole);
        
        adminRole = applicationTierIdentityManager.getRole(adminRole.getName());
        
        Assert.assertNotNull(adminRole);
        
        Realm testingRealm = createRealm();
        
        IdentityManager testingRealmIdentityManager = applicationTierIdentityManager.forRealm(testingRealm);

        adminRole = applicationTierIdentityManager.getRole(adminRole.getName());
        
        Assert.assertNotNull(adminRole);

        User someUser = new SimpleUser("someUser");
        
        testingRealmIdentityManager.add(someUser);
        
        testingRealmIdentityManager.grantRole(someUser, adminRole);
        
        Assert.assertTrue(testingRealmIdentityManager.hasRole(someUser, adminRole));
        Assert.assertTrue(applicationTierIdentityManager.hasRole(someUser, adminRole));
    }
    
    private Realm createRealm() {
        IdentityManager identityManager = getIdentityManager();
        
        Realm realm = identityManager.getRealm("Testing");
        
        if (realm != null) {
            identityManager.removeRealm(realm);
        }
        
        realm = new Realm("Testing");
        
        identityManager.createRealm(realm);
        return realm;
    }
    
    @Test
    public void testGroupsForTier() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Tier applicationTier = createApplicationTier();
        
        IdentityManager applicationTierIdentityManager = identityManager.forTier(applicationTier);
        
        Group testingGroup = new SimpleGroup("testingGroup");
        
        applicationTierIdentityManager.add(testingGroup);
        
        testingGroup = applicationTierIdentityManager.getGroup(testingGroup.getName());
        
        Assert.assertNotNull(testingGroup);
        Assert.assertNotNull(testingGroup);
        Assert.assertNotNull(testingGroup);
        Assert.assertNotNull(testingGroup.getPartition());
        Assert.assertEquals(applicationTier.getId(), testingGroup.getPartition().getId());
        
        testingGroup = identityManager.getGroup(testingGroup.getName());
        
        Assert.assertNull(testingGroup);
    }
}
