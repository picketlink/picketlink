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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.picketlink.idm.IdentityContext;
import org.picketlink.idm.IdentityContextFactory;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.DefaultIdentityContextFactory;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.internal.ResourceLocalJpaIdentityContextHandler;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.AbstractIdentityManagerTestCase;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.*;

/**
 * <p>Test case for the {@link org.picketlink.idm.model.Realm} management operations.</p>
 *
 * @author Bill Burke
 *
 */
public class JPAIdentityContextTestCase {

    private static final String TESTING_REALM_NAME = "Create-Testing";

    static IdentityContextFactory factory;
    @BeforeClass
    public static void initialize()
    {
        factory = createFactory();
        IdentityContext ctx = factory.createIdentityContext();
        ctx.createRealm(Realm.DEFAULT_REALM);
        ctx.createRealm(TESTING_REALM_NAME);
        ctx.close();
    }

    private static IdentityContextFactory createFactory() {
        ResourceLocalJpaIdentityContextHandler handler = new ResourceLocalJpaIdentityContextHandler("jpa-identity-store-realm-tests");
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .stores()
                .jpa()
                .identityClass(IdentityObject.class)
                .attributeClass(IdentityObjectAttribute.class)
                .relationshipClass(RelationshipObject.class)
                .relationshipIdentityClass(RelationshipIdentityObject.class)
                .relationshipAttributeClass(RelationshipObjectAttribute.class)
                .credentialClass(CredentialObject.class)
                .credentialAttributeClass(CredentialObjectAttribute.class)
                .partitionClass(PartitionObject.class)
                .supportAllFeatures()
                .setIdentityContextHandler(handler);

        IdentityConfiguration build = builder.build();
        return new DefaultIdentityContextFactory(build);
    }

    @AfterClass
    public static void cleanup() throws Exception
    {
        IdentityContext ctx = factory.createIdentityContext();
        Realm testingRealm = ctx.findRealm(TESTING_REALM_NAME);
        Realm defaultRealm = ctx.findRealm(Realm.DEFAULT_REALM);
        ctx.deleteRealm(testingRealm);
        ctx.deleteRealm(defaultRealm);
        factory.close();
    }

    protected IdentityContext ctx;

    @Before
    public void createContext()
    {
        ctx = factory.createIdentityContext();
    }

    @After
    public void closeContext()
    {
        ctx.close();
    }


    @Test
    public void testRealm() throws Exception {
        ctx.getTransaction().begin();
        Assert.assertTrue(ctx.getTransaction().isActive());
        ctx.createRealm("foo");
        Assert.assertNotNull(ctx.findRealm("foo"));
        ctx.getTransaction().rollback();
        Assert.assertFalse(ctx.getTransaction().isActive());
        Assert.assertNull(ctx.findRealm("foo"));
        Assert.assertFalse(ctx.getTransaction().isActive());
        ctx.getTransaction().begin();
        Assert.assertTrue(ctx.getTransaction().isActive());
        ctx.createRealm("foo");
        ctx.getTransaction().commit();
        Assert.assertFalse(ctx.getTransaction().isActive());
        Assert.assertNotNull(ctx.findRealm("foo"));
        Assert.assertFalse(ctx.getTransaction().isActive());
    }

    @Test
    public void testCreateUserInNewRealm() throws Exception {
        Realm realm = ctx.createRealm("Delete-Testing");
        IdentityManager testingRealmManager = ctx.createIdentityManager(realm);

        User realmUser = new SimpleUser("newUser");
        testingRealmManager.add(realmUser);


        realmUser = testingRealmManager.getUser(realmUser.getLoginName());

        assertNotNull(realmUser);
        assertNotNull(realmUser.getPartition());
        assertEquals(realm.getId(), realmUser.getPartition().getId());

        IdentityManager defaultIdentityManager = ctx.defaultIdentityManager();

        // the identitytype should not be associated with the DEFAULT realm
        realmUser = defaultIdentityManager.getUser(realmUser.getLoginName());

        assertNull(realmUser);

        ctx.deleteRealm(realm);
        assertNull(ctx.findRealm(realm.getId()));
    }


    @Test
    public void testCreateUsers() throws Exception {
        Realm realm = ctx.findRealm(TESTING_REALM_NAME);
        IdentityManager testingRealmManager = ctx.createIdentityManager(realm);
        User realmUser = new SimpleUser("realmUser");
        testingRealmManager.add(realmUser);


        realmUser = testingRealmManager.getUser(realmUser.getLoginName());

        assertNotNull(realmUser);
        assertNotNull(realmUser.getPartition());
        assertEquals(realm.getId(), realmUser.getPartition().getId());

        IdentityManager defaultIdentityManager = ctx.defaultIdentityManager();
        
        // the identitytype should not be associated with the DEFAULT realm
        realmUser = defaultIdentityManager.getUser(realmUser.getLoginName());
        
        assertNull(realmUser);
    }

    @Test
    public void testCreateUsersRollback() throws Exception {
        ctx.getTransaction().begin();
        Realm realm = ctx.findRealm(TESTING_REALM_NAME);
        IdentityManager testingRealmManager = ctx.createIdentityManager(realm);
        User realmUser = new SimpleUser("realmUser2");
        testingRealmManager.add(realmUser);


        realmUser = testingRealmManager.getUser(realmUser.getLoginName());


        assertNotNull(realmUser);
        assertNotNull(realmUser.getPartition());
        assertEquals(realm.getId(), realmUser.getPartition().getId());

        ctx.getTransaction().rollback();

        realmUser = testingRealmManager.getUser(realmUser.getLoginName());
        assertNull(realmUser);
    }


}
