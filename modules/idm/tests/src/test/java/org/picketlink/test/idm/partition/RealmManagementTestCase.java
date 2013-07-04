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

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.AbstractPartitionTestCase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * <p>Test case for the {@link Realm} management operations.</p>
 * 
 * @author Pedro Silva
 *
 */
public class RealmManagementTestCase extends AbstractPartitionTestCase<Realm> {

    private static final String TESTING_REALM_NAME = "PicketLink Realm";

    @Override
    protected Realm createPartition() {
        Realm realm = new Realm(TESTING_REALM_NAME);

        getPartitionManager().add(realm, "default");

        return realm;
    }

    @Override
    protected Realm getPartition() {
        return getPartitionManager().getPartition(Realm.class, TESTING_REALM_NAME);
    }

    @Test (expected=SecurityConfigurationException.class)
    @Ignore
    public void testUseNonExistentRealm() throws Exception {
        IdentityManager identityManager = getPartitionManager().createIdentityManager(new Realm("Not Configured Realm"));
        identityManager.add(new User("mary"));
    }

    @Test
    @Ignore
    public void testCreateUsers() throws Exception {
        Realm realm = getPartitionManager().getPartition(Realm.class, TESTING_REALM_NAME);
        
        User realmUser = createUser("realmUser", realm);
        
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);
        
        realmUser = testingRealmManager.getUser(realmUser.getLoginName());
        
        assertNotNull(realmUser);
        assertNotNull(realmUser.getPartition());
        assertEquals(realm.getId(), realmUser.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        realmUser = defaultIdentityManager.getUser(realmUser.getLoginName());
        
        assertNull(realmUser);
    }
    
    @Test
    @Ignore
    public void testCreateSameUserDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        User defaultRealmUser = new User("commonName");
        
        defaultIdentityManager.add(defaultRealmUser);
        
        try {
            // we can not add this user with the same login name
            defaultIdentityManager.add(new User(defaultRealmUser.getLoginName()));
            fail();
        } catch (Exception e) {
        }
        
        defaultRealmUser = defaultIdentityManager.getUser(defaultRealmUser.getLoginName());
        
        assertNotNull(defaultRealmUser);

        Realm realm = new Realm(TESTING_REALM_NAME);
        
        User testingRealmUser = createUser("commonName", realm);
        
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);

        testingRealmUser = testingRealmManager.getUser(testingRealmUser.getLoginName());
        
        assertNotNull(testingRealmUser);
        assertFalse(defaultRealmUser.getId().equals(testingRealmUser.getId()));
    }
    
    @Test
    @Ignore
    public void testCreateSameRoleDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Role defaultRealmRole = new Role("commonName");
        
        defaultIdentityManager.add(defaultRealmRole);
        
        try {
            // we can not add this role with the same name
            defaultIdentityManager.add(new Role(defaultRealmRole.getName()));
            fail();
        } catch (Exception e) {
        }
        
        defaultRealmRole = defaultIdentityManager.getRole(defaultRealmRole.getName());
        
        assertNotNull(defaultRealmRole);

        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Role testingRealmRole = createRole("commonName", realm);
        
        // get a IdentityManager instance for the given realm and associate the Role with the realm
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);

        testingRealmRole = testingRealmManager.getRole(testingRealmRole.getName());
        
        assertNotNull(testingRealmRole);
        assertFalse(defaultRealmRole.getId().equals(testingRealmRole.getId()));
    }
 
    @Test
    @Ignore
    public void testCreateSameGroupDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        Group defaultRealmGroup = new Group("commonName");
        
        defaultIdentityManager.add(defaultRealmGroup);
        
        try {
            // we can not add this user with the same name or path
            defaultIdentityManager.add(new Group(defaultRealmGroup.getName()));
            fail();
        } catch (Exception e) {
        }
        
        defaultRealmGroup = defaultIdentityManager.getGroup(defaultRealmGroup.getName());
        
        assertNotNull(defaultRealmGroup);

        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Group testingRealmGroup = createGroup("commonName", null, realm);
        
        // get a IdentityManager instance for the given realm and associate the Group with the realm
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);

        testingRealmGroup = testingRealmManager.getGroup(testingRealmGroup.getName());
        
        assertNotNull(testingRealmGroup);
        assertFalse(defaultRealmGroup.getId().equals(testingRealmGroup.getId()));
    }
    
    @Test
    @Ignore
    public void testCreateRoles() throws Exception {
        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Role testingRole = createRole("testingRole", realm);
        
        testingRole = getPartitionManager().createIdentityManager(realm).getRole(testingRole.getName());
        
        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(realm.getId(), testingRole.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        testingRole = defaultIdentityManager.getRole(testingRole.getName());
        
        assertNull(testingRole);
    }
    
    @Test
    @Ignore
    public void testCreateGroups() throws Exception {
        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Group testingGroup = createGroup("testingRealmGroup", null, realm);
        
        testingGroup = getPartitionManager().createIdentityManager(realm).getGroup(testingGroup.getName());
        
        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(realm.getId(), testingGroup.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        testingGroup = defaultIdentityManager.getGroup(testingGroup.getName());
        
        assertNull(testingGroup);
    }

//    @Test
//    public void testRelationships() throws Exception {
//        User defaultRealmUser = new User("defaultRealmUser");
//        Role defaultRealmRole = new Role("defaultRealmRole");
//        Group defaultRealmGroup = new Group("defaultRealmGroup");
//
//        PartitionManager partitionManager = getPartitionManager();
//        IdentityManager defaultIdentityManager = getIdentityManager();
//
//        defaultIdentityManager.add(defaultRealmUser);
//        defaultIdentityManager.add(defaultRealmRole);
//        defaultIdentityManager.add(defaultRealmGroup);
//
//        partitionManager.grantRole(defaultRealmUser, defaultRealmRole);
//        partitionManager.addToGroup(defaultRealmUser, defaultRealmGroup);
//        partitionManager.grantGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup);
//
//        assertTrue(partitionManager.hasRole(defaultRealmUser, defaultRealmRole));
//        assertTrue(partitionManager.isMember(defaultRealmUser, defaultRealmGroup));
//        assertTrue(partitionManager.hasGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup));
//
//        Realm realm = new Realm(TESTING_REALM_NAME);
//
//        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);
//
//        assertFalse(partitionManager.hasRole(defaultRealmUser, defaultRealmRole));
//        assertFalse(partitionManager.isMember(defaultRealmUser, defaultRealmGroup));
//        assertFalse(partitionManager.hasGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup));
//
//        User testingRealmUser = createUser("testingRealmUser", realm);
//        Role testingRealmRole = createRole("testingRealmRole", realm);
//        Group testingRealmGroup = createGroup("testingRealmGroup", null, realm);
//
//        partitionManager.grantRole(testingRealmUser, testingRealmRole);
//        partitionManager.addToGroup(testingRealmUser, testingRealmGroup);
//        partitionManager.grantGroupRole(testingRealmUser, testingRealmRole, testingRealmGroup);
//
//        assertTrue(partitionManager.hasRole(testingRealmUser, testingRealmRole));
//        assertTrue(partitionManager.isMember(testingRealmUser, testingRealmGroup));
//        assertTrue(partitionManager.hasGroupRole(testingRealmUser, testingRealmRole, testingRealmGroup));
//
//        assertFalse(partitionManager.hasRole(testingRealmUser, testingRealmRole));
//        assertFalse(partitionManager.isMember(testingRealmUser, testingRealmGroup));
//        assertFalse(partitionManager.hasGroupRole(testingRealmUser, testingRealmRole, testingRealmGroup));
//
//        assertFalse(partitionManager.hasRole(defaultRealmUser, testingRealmRole));
//        assertFalse(partitionManager.hasRole(testingRealmUser, defaultRealmRole));
//
//        assertFalse(partitionManager.isMember(defaultRealmUser, testingRealmGroup));
//        assertFalse(partitionManager.isMember(testingRealmUser, defaultRealmGroup));
//
//        assertFalse(partitionManager.hasGroupRole(defaultRealmUser, testingRealmRole, defaultRealmGroup));
//        assertFalse(partitionManager.hasGroupRole(testingRealmUser, defaultRealmRole, testingRealmGroup));
//    }

}
