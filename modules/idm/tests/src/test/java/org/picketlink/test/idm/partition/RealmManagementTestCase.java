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
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;

import static org.junit.Assert.*;

/**
 * <p>Test case for the {@link Realm} management operations.</p>
 * 
 * @author Pedro Silva
 *
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
public class RealmManagementTestCase extends AbstractPartitionTestCase<Realm> {

    private static final String TESTING_REALM_NAME = "PicketLink Realm";

    public RealmManagementTestCase(IdentityConfigurationTester builder) {
        super(builder);
    }

    @Override
    protected Realm createPartition() {
        Realm realm = new Realm(TESTING_REALM_NAME);

        if (getPartitionManager().getPartition(realm.getClass(), realm.getName()) != null) {
            getPartitionManager().remove(realm);
        }

        getPartitionManager().add(realm);

        return realm;
    }

    @Override
    protected Realm getPartition() {
        return getPartitionManager().getPartition(Realm.class, TESTING_REALM_NAME);
    }

    @Test (expected=IdentityManagementException.class)
    public void testUseNonExistentRealm() throws Exception {
        IdentityManager identityManager = getPartitionManager().createIdentityManager(new Realm("Not Configured Realm"));
        identityManager.add(new User("mary"));
    }

    @Test
    public void testCreateUsers() throws Exception {
        Realm realm = createPartition();
        
        User realmUser = createUser("realmUser", realm);
        
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);
        
        realmUser = BasicModel.getUser(testingRealmManager, realmUser.getLoginName());
        
        assertNotNull(realmUser);
        assertNotNull(realmUser.getPartition());
        assertEquals(realm.getId(), realmUser.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        realmUser = BasicModel.getUser(defaultIdentityManager, realmUser.getLoginName());
        
        assertNull(realmUser);
    }
    
    @Test
    public void testCreateSameUserDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        User defaultRealmUser = new User("commonName");
        
        defaultIdentityManager.add(defaultRealmUser);
        
        try {
            // we can not add this user with the same login name
            defaultIdentityManager.add(new User(defaultRealmUser.getLoginName()));
            fail();
        } catch (IdentityManagementException e) {
        }
        
        defaultRealmUser = BasicModel.getUser(defaultIdentityManager, defaultRealmUser.getLoginName());
        
        assertNotNull(defaultRealmUser);

        Realm realm = createPartition();
        
        User testingRealmUser = createUser("commonName", realm);
        
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);

        testingRealmUser = BasicModel.getUser(testingRealmManager, testingRealmUser.getLoginName());
        
        assertNotNull(testingRealmUser);
        assertFalse(defaultRealmUser.getId().equals(testingRealmUser.getId()));
    }
    
    @Test
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
        
        defaultRealmRole = BasicModel.getRole(defaultIdentityManager, defaultRealmRole.getName());
        
        assertNotNull(defaultRealmRole);

        Realm realm = createPartition();
        
        Role testingRealmRole = createRole("commonName", realm);
        
        // get a IdentityManager instance for the given realm and associate the Role with the realm
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);

        testingRealmRole = BasicModel.getRole(testingRealmManager, testingRealmRole.getName());
        
        assertNotNull(testingRealmRole);
        assertFalse(defaultRealmRole.getId().equals(testingRealmRole.getId()));
    }
 
    @Test
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
        
        defaultRealmGroup = BasicModel.getGroup(defaultIdentityManager, defaultRealmGroup.getName());
        
        assertNotNull(defaultRealmGroup);

        Realm realm = createPartition();
        
        Group testingRealmGroup = createGroup("commonName", null, realm);
        
        // get a IdentityManager instance for the given realm and associate the Group with the realm
        IdentityManager testingRealmManager = getPartitionManager().createIdentityManager(realm);

        testingRealmGroup = BasicModel.getGroup(testingRealmManager, testingRealmGroup.getName());
        
        assertNotNull(testingRealmGroup);
        assertFalse(defaultRealmGroup.getId().equals(testingRealmGroup.getId()));
    }
    
    @Test
    public void testCreateRoles() throws Exception {
        Realm realm = createPartition();
        
        Role testingRole = createRole("testingRole", realm);
        
        testingRole = BasicModel.getRole(getPartitionManager().createIdentityManager(realm), testingRole.getName());
        
        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(realm.getId(), testingRole.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        testingRole = BasicModel.getRole(defaultIdentityManager, testingRole.getName());
        
        assertNull(testingRole);
    }
    
    @Test
    public void testCreateGroups() throws Exception {
        Realm realm = createPartition();
        
        Group testingGroup = createGroup("testingRealmGroup", null, realm);
        
        testingGroup = BasicModel.getGroup(getPartitionManager().createIdentityManager(realm), testingGroup.getName());
        
        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(realm.getId(), testingGroup.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        testingGroup = BasicModel.getGroup(defaultIdentityManager, testingGroup.getName());
        
        assertNull(testingGroup);
    }

    @Test
    public void testRelationships() throws Exception {
        User defaultRealmUser = new User("defaultRealmUser");
        Role defaultRealmRole = new Role("defaultRealmRole");
        Group defaultRealmGroup = new Group("defaultRealmGroup");

        IdentityManager defaultIdentityManager = getIdentityManager();

        defaultIdentityManager.add(defaultRealmUser);
        defaultIdentityManager.add(defaultRealmRole);
        defaultIdentityManager.add(defaultRealmGroup);

        RelationshipManager relationshipManager = getPartitionManager().createRelationshipManager();

        BasicModel.grantRole(relationshipManager, defaultRealmUser, defaultRealmRole);
        BasicModel.addToGroup(relationshipManager, defaultRealmUser, defaultRealmGroup);
        BasicModel.grantGroupRole(relationshipManager, defaultRealmUser, defaultRealmRole, defaultRealmGroup);

        assertTrue(BasicModel.hasRole(relationshipManager, defaultRealmUser, defaultRealmRole));
        assertTrue(BasicModel.isMember(relationshipManager, defaultRealmUser, defaultRealmGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, defaultRealmUser, defaultRealmRole, defaultRealmGroup));

        Realm realm = getPartitionManager().getPartition(Realm.class, TESTING_REALM_NAME);

        User testingRealmUser = createUser("testingRealmUser", realm);
        Role testingRealmRole = createRole("testingRealmRole", realm);
        Group testingRealmGroup = createGroup("testingRealmGroup", null, realm);

        BasicModel.grantRole(relationshipManager, testingRealmUser, testingRealmRole);
        BasicModel.addToGroup(relationshipManager, testingRealmUser, testingRealmGroup);
        BasicModel.grantGroupRole(relationshipManager, testingRealmUser, testingRealmRole, testingRealmGroup);

        assertTrue(BasicModel.hasRole(relationshipManager, testingRealmUser, testingRealmRole));
        assertTrue(BasicModel.isMember(relationshipManager, testingRealmUser, testingRealmGroup));
        assertTrue(BasicModel.hasGroupRole(relationshipManager, testingRealmUser, testingRealmRole, testingRealmGroup));

        assertFalse(BasicModel.hasRole(relationshipManager, defaultRealmUser, testingRealmRole));
        assertFalse(BasicModel.hasRole(relationshipManager, testingRealmUser, defaultRealmRole));

        assertFalse(BasicModel.isMember(relationshipManager, defaultRealmUser, testingRealmGroup));
        assertFalse(BasicModel.isMember(relationshipManager, testingRealmUser, defaultRealmGroup));

        assertFalse(BasicModel.hasGroupRole(relationshipManager, defaultRealmUser, testingRealmRole, defaultRealmGroup));
        assertFalse(BasicModel.hasGroupRole(relationshipManager, testingRealmUser, defaultRealmRole, testingRealmGroup));
    }

}
