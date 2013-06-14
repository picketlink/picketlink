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

package org.picketlink.test.idm.individual;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.file.internal.FileUtils;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

import java.io.File;

import static org.junit.Assert.*;

/**
 * <p>Test case for the {@link org.picketlink.idm.model.Realm} management operations.</p>
 *
 * @author Pedro Silva
 *
 */
public class FileRealmCreateTestCase extends AbstractIdentityManagerTestCase {

    private static final String TESTING_REALM_NAME = "Testing";

    private static IdentityManagerFactory factory;

    @BeforeClass
    public static void before() throws Exception
    {
        after();
        factory = createFactory();
        factory.createRealm(TESTING_REALM_NAME); // this tests the creation
    }

    private static IdentityManagerFactory createFactory() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .stores()
                .file()
                .workingDirectory("/tmp/realm-create-test")
                .preserveState(true)
                .addRealm(Realm.DEFAULT_REALM)
                .supportAllFeatures();

        return new IdentityManagerFactory(builder.build());
    }

    @Override
    public IdentityManagerFactory getIdentityManagerFactory() {
        return factory;
    }

    @Override
    public void setIdentityManagerFactory(IdentityManagerFactory factory) {
        throw new RuntimeException("NOT ALLOWED");
    }

    @AfterClass
    public static void after() throws Exception
    {
        File file = new File("/tmp/realm-create-test");
        FileUtils.delete(file);
        Thread.sleep(100); // my windows machine seems to have delays on deleting files sometimes
    }
    @Test
    public void findRealm() {
        Assert.assertNotNull(getIdentityManagerFactory().createRealm("foo"));
        Assert.assertNotNull(createFactory().findRealm("foo"));
    }

    @Test (expected=SecurityConfigurationException.class)
    public void testUseNonExistentRealm() throws Exception {
        IdentityManager identityManager = getIdentityManagerFactory().createIdentityManager(new Realm("Not Configured Realm"));
        identityManager.add(new SimpleUser("mary"));
    }

    @Test
    public void testCreateUsers() throws Exception {
        Realm realm = getIdentityManagerFactory().getRealm(TESTING_REALM_NAME);
        
        User realmUser = createUser("realmUser", realm);
        
        IdentityManager testingRealmManager = getIdentityManagerFactory().createIdentityManager(realm);
        
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
    public void testCreateUserInNewRealm() throws Exception {
        Realm realm = getIdentityManagerFactory().createRealm("Delete-Testing");

        User realmUser = createUser("newUser", realm);

        IdentityManager testingRealmManager = getIdentityManagerFactory().createIdentityManager(realm);

        realmUser = testingRealmManager.getUser(realmUser.getLoginName());

        assertNotNull(realmUser);
        assertNotNull(realmUser.getPartition());
        assertEquals(realm.getId(), realmUser.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();

        // the identitytype should not be associated with the DEFAULT realm
        realmUser = defaultIdentityManager.getUser(realmUser.getLoginName());

        assertNull(realmUser);

        getIdentityManagerFactory().deleteRealm(realm);
        assertNull(getIdentityManagerFactory().findRealm(realm.getId()));
    }


    @Test
    public void testCreateSameUserDifferentRealms() throws Exception {
        IdentityManager defaultIdentityManager = getIdentityManager();
        
        User defaultRealmUser = new SimpleUser("commonName");
        
        defaultIdentityManager.add(defaultRealmUser);
        
        try {
            // we can not add this user with the same login name
            defaultIdentityManager.add(new SimpleUser(defaultRealmUser.getLoginName()));
            fail();
        } catch (Exception e) {
        }
        
        defaultRealmUser = defaultIdentityManager.getUser(defaultRealmUser.getLoginName());
        
        assertNotNull(defaultRealmUser);

        Realm realm = new Realm(TESTING_REALM_NAME);
        
        User testingRealmUser = createUser("commonName", realm);
        
        IdentityManager testingRealmManager = getIdentityManagerFactory().createIdentityManager(realm);

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
            // we can not add this role with the same name
            defaultIdentityManager.add(new SimpleRole(defaultRealmRole.getName()));
            fail();
        } catch (Exception e) {
        }
        
        defaultRealmRole = defaultIdentityManager.getRole(defaultRealmRole.getName());
        
        assertNotNull(defaultRealmRole);

        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Role testingRealmRole = createRole("commonName", realm);
        
        // get a IdentityManager instance for the given realm and associate the Role with the realm
        IdentityManager testingRealmManager = getIdentityManagerFactory().createIdentityManager(realm);

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
            // we can not add this user with the same name or path
            defaultIdentityManager.add(new SimpleGroup(defaultRealmGroup.getName()));
            fail();
        } catch (Exception e) {
        }
        
        defaultRealmGroup = defaultIdentityManager.getGroup(defaultRealmGroup.getName());
        
        assertNotNull(defaultRealmGroup);

        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Group testingRealmGroup = createGroup("commonName", null, realm);
        
        // get a IdentityManager instance for the given realm and associate the Group with the realm
        IdentityManager testingRealmManager = getIdentityManagerFactory().createIdentityManager(realm);

        testingRealmGroup = testingRealmManager.getGroup(testingRealmGroup.getName());
        
        assertNotNull(testingRealmGroup);
        assertFalse(defaultRealmGroup.getId().equals(testingRealmGroup.getId()));
    }
    
    @Test
    public void testCreateRoles() throws Exception {
        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Role testingRole = createRole("testingRole", realm);
        
        testingRole = getIdentityManagerFactory().createIdentityManager(realm).getRole(testingRole.getName());
        
        assertNotNull(testingRole);
        assertNotNull(testingRole.getPartition());
        assertEquals(realm.getId(), testingRole.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        testingRole = defaultIdentityManager.getRole(testingRole.getName());
        
        assertNull(testingRole);
    }
    
    @Test
    public void testCreateGroups() throws Exception {
        Realm realm = new Realm(TESTING_REALM_NAME);
        
        Group testingGroup = createGroup("testingRealmGroup", null, realm);
        
        testingGroup = getIdentityManagerFactory().createIdentityManager(realm).getGroup(testingGroup.getName());
        
        assertNotNull(testingGroup);
        assertNotNull(testingGroup.getPartition());
        assertEquals(realm.getId(), testingGroup.getPartition().getId());

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        testingGroup = defaultIdentityManager.getGroup(testingGroup.getName());
        
        assertNull(testingGroup);
    }

    @Test
    public void testRelationships() throws Exception {
        User defaultRealmUser = new SimpleUser("defaultRealmUser");
        Role defaultRealmRole = new SimpleRole("defaultRealmRole");
        Group defaultRealmGroup = new SimpleGroup("defaultRealmGroup");

        IdentityManager defaultIdentityManager = getIdentityManager();
        
        defaultIdentityManager.add(defaultRealmUser);
        defaultIdentityManager.add(defaultRealmRole);
        defaultIdentityManager.add(defaultRealmGroup);
        
        defaultIdentityManager.grantRole(defaultRealmUser, defaultRealmRole);
        defaultIdentityManager.addToGroup(defaultRealmUser, defaultRealmGroup);
        defaultIdentityManager.grantGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup);

        assertTrue(defaultIdentityManager.hasRole(defaultRealmUser, defaultRealmRole));
        assertTrue(defaultIdentityManager.isMember(defaultRealmUser, defaultRealmGroup));
        assertTrue(defaultIdentityManager.hasGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup));

        Realm realm = new Realm(TESTING_REALM_NAME);
        
        IdentityManager testingRealmManager = getIdentityManagerFactory().createIdentityManager(realm);
        
        assertFalse(testingRealmManager.hasRole(defaultRealmUser, defaultRealmRole));
        assertFalse(testingRealmManager.isMember(defaultRealmUser, defaultRealmGroup));
        assertFalse(testingRealmManager.hasGroupRole(defaultRealmUser, defaultRealmRole, defaultRealmGroup));
        
        User testingRealmUser = createUser("testingRealmUser", realm);
        Role testingRealmRole = createRole("testingRealmRole", realm);
        Group testingRealmGroup = createGroup("testingRealmGroup", null, realm);
        
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

}
