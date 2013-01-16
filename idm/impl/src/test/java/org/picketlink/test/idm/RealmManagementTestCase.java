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
import org.picketlink.idm.model.User;

/**
 * @author Pedro Silva
 *
 */
public class RealmManagementTestCase extends AbstractIdentityManagerTestCase {

    @Test
    public void testCreateRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Realm realm = createRealm();
        
        realm = identityManager.getRealm(realm.getName());
        
        Assert.assertNotNull(realm);
        Assert.assertEquals("Testing", realm.getName());
        Assert.assertEquals(Realm.KEY_PREFIX + "Testing", realm.getKey());
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
    public void testRemoveRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Realm realm = createRealm();
        
        Assert.assertNotNull(realm);
        
        identityManager.removeRealm(realm);
        
        realm = identityManager.getRealm(realm.getName());
        
        Assert.assertNull(realm);
    }
    
    @Test
    public void testUsersForRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Realm realm = createRealm();
        
        IdentityManager testingIdentityManager = identityManager.forRealm(realm);
        
        User testingUser = new SimpleUser("testingUser");
        
        testingIdentityManager.add(testingUser);
        
        testingUser = testingIdentityManager.getUser(testingUser.getLoginName());
        
        Assert.assertNotNull(testingUser);
        Assert.assertNotNull(testingUser.getPartition());
        Assert.assertEquals(realm.getId(), testingUser.getPartition().getId());
        
        testingUser = identityManager.getUser(testingUser.getLoginName());
        
        Assert.assertNull(testingUser);
        
        identityManager.removeRealm(realm);
    }
    
    @Test
    public void testRolesForRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Realm realm = createRealm();
        
        IdentityManager testingIdentityManager = identityManager.forRealm(realm);
        
        Role testingRole = new SimpleRole("testingRole");
        
        testingIdentityManager.add(testingRole);
        
        testingRole = testingIdentityManager.getRole(testingRole.getName());
        
        Assert.assertNotNull(testingRole);
        Assert.assertNotNull(testingRole);
        Assert.assertNotNull(testingRole.getPartition());
        Assert.assertEquals(realm.getId(), testingRole.getPartition().getId());

        testingRole = identityManager.getRole(testingRole.getName());
        
        Assert.assertNull(testingRole);
    }
    
    @Test
    public void testGroupsForRealm() throws Exception {
        IdentityManager identityManager = getIdentityManager();
        
        Realm realm = createRealm();
        
        IdentityManager testingIdentityManager = identityManager.forRealm(realm);
        
        Group testingGroup = new SimpleGroup("testingGroup");
        
        testingIdentityManager.add(testingGroup);
        
        testingGroup = testingIdentityManager.getGroup(testingGroup.getName());
        
        Assert.assertNotNull(testingGroup);
        Assert.assertNotNull(testingGroup);
        Assert.assertNotNull(testingGroup.getPartition());
        Assert.assertEquals(realm.getId(), testingGroup.getPartition().getId());
        
        testingGroup = identityManager.getGroup(testingGroup.getName());
        
        Assert.assertNull(testingGroup);
    }
}
