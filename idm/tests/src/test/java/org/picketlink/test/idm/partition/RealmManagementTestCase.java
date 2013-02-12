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

package org.picketlink.test.idm.partition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

/**
 * <p>Test case for the {@link Realm} management operations.</p>
 * 
 * @author Pedro Silva
 *
 */
public class RealmManagementTestCase extends AbstractIdentityManagerTestCase {

    private static final String TESTING_REALM_NAME = "Testing";

    @Test
    public void testCreate() throws Exception {
        Realm realm = createRealm();

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        realm = defaultIdentityManager.getRealm(realm.getName());
        
        assertNotNull(realm);
        assertEquals(TESTING_REALM_NAME, realm.getName());
        assertEquals(Realm.KEY_PREFIX + TESTING_REALM_NAME, realm.getKey());
    }
    
    @Test
    public void testGetDefaultRealm() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Realm defaultRealm = defaultIdentityManager.getRealm(Realm.DEFAULT_REALM);
        
        assertNotNull(defaultRealm);
        assertEquals(Realm.DEFAULT_REALM, defaultRealm.getName());
    }
    
    @Test (expected=IdentityManagementException.class)
    public void testCreateWithNullArgument() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        defaultIdentityManager.createRealm(null);
    }
    
    @Test (expected=InstantiationError.class)
    public void testCreateWithNullName() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        defaultIdentityManager.createRealm(new Realm(null));
    }

    @Test
    public void testRemove() throws Exception {
        Realm realm = createRealm();
        
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        defaultIdentityManager.removeRealm(realm);
        
        realm = defaultIdentityManager.getRealm(realm.getName());
        
        assertNull(realm);
    }
    
    @Test (expected=IdentityManagementException.class)
    public void testRemoveWithUsers() throws Exception {
        Realm realm = createRealm();
        
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        IdentityManager testingIdentityManager = defaultIdentityManager.forRealm(realm);
        
        User testingUser = new SimpleUser("testingUser");
        
        // should throw an exception because the current realm has IdentityTypes associated.
        testingIdentityManager.add(testingUser);
        
        defaultIdentityManager.removeRealm(realm);
    }

    @Test (expected=IdentityManagementException.class)
    public void testRemoveWithRoles() throws Exception {
        Realm realm = createRealm();
        
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        IdentityManager testingIdentityManager = defaultIdentityManager.forRealm(realm);
        
        Role testingRole = new SimpleRole("testingRole");
        
        // should throw an exception because the current realm has IdentityTypes associated.
        testingIdentityManager.add(testingRole);
        
        defaultIdentityManager.removeRealm(realm);
    }
    
    @Test (expected=IdentityManagementException.class)
    public void testRemoveWithGroups() throws Exception {
        Realm realm = createRealm();
        
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        IdentityManager testingIdentityManager = defaultIdentityManager.forRealm(realm);
        
        Group testingGroup = new SimpleGroup("testingGroup");
        
        // should throw an exception because the current realm has IdentityTypes associated.
        testingIdentityManager.add(testingGroup);
        
        defaultIdentityManager.removeRealm(realm);
    }

    @Test
    public void testCreateUsers() throws Exception {
        Realm realm = createRealm();

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        User realmUser = new SimpleUser("realmUser");
        
        // get a IdentityManager instance for the given realm and associate the user with the realm
        defaultIdentityManager.forRealm(realm).add(realmUser);
        
        realmUser = defaultIdentityManager.forRealm(realm).getUser(realmUser.getLoginName());
        
        assertNotNull(realmUser);
        assertNotNull(realmUser.getPartition());
        assertEquals(realm.getId(), realmUser.getPartition().getId());
        
        // the identitytype should not be associated with the DEFAULT realm
        realmUser = defaultIdentityManager.getUser(realmUser.getLoginName());
        
        assertNull(realmUser);
    }
    
    @Test
    public void testCreateSameUserDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        User defaultRealmUser = new SimpleUser("commonName");
        
        defaultIdentityManager.add(defaultRealmUser);
        
        try {
            defaultIdentityManager.add(new SimpleUser(defaultRealmUser.getLoginName()));
            fail();
        } catch (Exception e) {
        }
        
        Realm realm = createRealm();
        
        User testingRealmUser = new SimpleUser("commonName");
        
        // get a IdentityManager instance for the given realm and associate the user with the realm
        IdentityManager testingRealmManager = defaultIdentityManager.forRealm(realm);
        
        testingRealmManager.add(testingRealmUser);
        
        defaultRealmUser = defaultIdentityManager.getUser(defaultRealmUser.getLoginName());
        
        assertNotNull(defaultRealmUser);

        testingRealmUser = testingRealmManager.getUser(testingRealmUser.getLoginName());
        
        assertNotNull(testingRealmUser);
        
        assertFalse(defaultRealmUser.getId().equals(testingRealmUser.getId()));
    }
    
    @Test
    public void testCreateSameRoleDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Role defaultRealmRole = new SimpleRole("commonName");
        
        defaultIdentityManager.add(defaultRealmRole);
        
        try {
            defaultIdentityManager.add(new SimpleRole(defaultRealmRole.getName()));
            fail();
        } catch (Exception e) {
        }
        
        Realm realm = createRealm();
        
        Role testingRealmRole = new SimpleRole("commonName");
        
        // get a IdentityManager instance for the given realm and associate the Role with the realm
        IdentityManager testingRealmManager = defaultIdentityManager.forRealm(realm);
        
        testingRealmManager.add(testingRealmRole);
        
        defaultRealmRole = defaultIdentityManager.getRole(defaultRealmRole.getName());
        
        assertNotNull(defaultRealmRole);

        testingRealmRole = testingRealmManager.getRole(testingRealmRole.getName());
        
        assertNotNull(testingRealmRole);
        
        assertFalse(defaultRealmRole.getId().equals(testingRealmRole.getId()));
    }
    
    @Test
    public void testCreateSameGroupDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Group defaultRealmGroup = new SimpleGroup("commonName");
        
        defaultIdentityManager.add(defaultRealmGroup);
        
        try {
            defaultIdentityManager.add(new SimpleGroup(defaultRealmGroup.getName()));
            fail();
        } catch (Exception e) {
        }
        
        Realm realm = createRealm();
        
        Group testingRealmGroup = new SimpleGroup("commonName");
        
        // get a IdentityManager instance for the given realm and associate the Group with the realm
        IdentityManager testingRealmManager = defaultIdentityManager.forRealm(realm);
        
        testingRealmManager.add(testingRealmGroup);
        
        defaultRealmGroup = defaultIdentityManager.getGroup(defaultRealmGroup.getName());
        
        assertNotNull(defaultRealmGroup);

        testingRealmGroup = testingRealmManager.getGroup(testingRealmGroup.getName());
        
        assertNotNull(testingRealmGroup);
        
        assertFalse(defaultRealmGroup.getId().equals(testingRealmGroup.getId()));
    }
    
    @Test
    public void testCreateRoles() throws Exception {
        Realm realm = createRealm();
        
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Role testingRole = new SimpleRole("testingRole");
        
        // get a IdentityManager instance for the given realm and associate the role with the current realm
        defaultIdentityManager.forRealm(realm).add(testingRole);
        
        testingRole = defaultIdentityManager.forRealm(realm).getRole(testingRole.getName());
        
        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(realm.getId(), testingRole.getPartition().getId());

        // the identitytype should not be associated with the DEFAULT realm
        testingRole = defaultIdentityManager.getRole(testingRole.getName());
        
        assertNull(testingRole);
    }
    
    @Test
    public void testCreateGroups() throws Exception {
        Realm realm = createRealm();
        
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Group testingGroup = new SimpleGroup("testingRealmGroup");
        
        // get a IdentityManager instance for the given realm and associate the group with the current realm
        defaultIdentityManager.forRealm(realm).add(testingGroup);
        
        testingGroup = defaultIdentityManager.forRealm(realm).getGroup(testingGroup.getName());
        
        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(realm.getId(), testingGroup.getPartition().getId());
        
        // the identitytype should not be associated with the DEFAULT realm
        testingGroup = defaultIdentityManager.getGroup(testingGroup.getName());
        
        assertNull(testingGroup);
    }
    
    @Test
    public void testRelationships() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        User defaultRealmUser = new SimpleUser("defaultRealmUser");
        Role defaultRealmRole = new SimpleRole("defaultRealmRole");
        Group defaultRealmGroup = new SimpleGroup("defaultRealmGroup");
        
        defaultIdentityManager.add(defaultRealmUser);
        defaultIdentityManager.add(defaultRealmRole);
        defaultIdentityManager.add(defaultRealmGroup);
        
        defaultIdentityManager.grantRole(defaultRealmUser, defaultRealmRole);
        defaultIdentityManager.addToGroup(defaultRealmUser, defaultRealmGroup);
        defaultIdentityManager.grantGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup);

        assertTrue(defaultIdentityManager.hasRole(defaultRealmUser, defaultRealmRole));
        assertTrue(defaultIdentityManager.isMember(defaultRealmUser, defaultRealmGroup));
        assertTrue(defaultIdentityManager.hasGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup));

        Realm realm = createRealm();
        
        IdentityManager testingRealmManager = defaultIdentityManager.forRealm(realm);
        
        assertFalse(testingRealmManager.hasRole(defaultRealmUser, defaultRealmRole));
        assertFalse(testingRealmManager.isMember(defaultRealmUser, defaultRealmGroup));
        assertFalse(testingRealmManager.hasGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup));
        
        User testingRealmUser = new SimpleUser("testingRealmUser");
        Role testingRealmRole = new SimpleRole("testingRealmRole");
        Group testingRealmGroup = new SimpleGroup("testingRealmGroup");
        
        testingRealmManager.add(testingRealmUser);
        testingRealmManager.add(testingRealmRole);
        testingRealmManager.add(testingRealmGroup);
        
        testingRealmManager.grantRole(testingRealmUser, testingRealmRole);
        testingRealmManager.addToGroup(testingRealmUser, testingRealmGroup);
        testingRealmManager.grantGroupRole(testingRealmUser, testingRealmRole, testingRealmGroup);
        
        assertTrue(testingRealmManager.hasRole(testingRealmUser, testingRealmRole));
        assertTrue(testingRealmManager.isMember(testingRealmUser, testingRealmGroup));
        assertTrue(testingRealmManager.hasGroupRole(testingRealmUser, testingRealmRole, testingRealmGroup));
        
        assertFalse(defaultIdentityManager.hasRole(testingRealmUser, testingRealmRole));
        assertFalse(defaultIdentityManager.isMember(testingRealmUser, testingRealmGroup));
        assertFalse(defaultIdentityManager.hasGroupRole(testingRealmUser, testingRealmRole, testingRealmGroup));
        
        assertFalse(defaultIdentityManager.hasRole(defaultRealmUser, testingRealmRole));
        assertFalse(defaultIdentityManager.hasRole(testingRealmUser, defaultRealmRole));
        
        assertFalse(defaultIdentityManager.isMember(defaultRealmUser, testingRealmGroup));
        assertFalse(defaultIdentityManager.isMember(testingRealmUser, defaultRealmGroup));

        assertFalse(defaultIdentityManager.hasGroupRole(defaultRealmUser, testingRealmRole, defaultRealmGroup));
        assertFalse(defaultIdentityManager.hasGroupRole(testingRealmUser, defaultRealmRole, testingRealmGroup));

    }
    
    private Realm createRealm() {
        IdentityManager identityManager = getIdentityManager();
        Realm realm = identityManager.getRealm(TESTING_REALM_NAME);
        
        if (realm == null) {
            realm = new Realm(TESTING_REALM_NAME);
            identityManager.createRealm(realm);
        }
        
        return realm;
    }

}
