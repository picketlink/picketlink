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

package org.picketlink.test.idm.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.picketlink.config.idm.XMLConfigurationProvider;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPStoreConfigurationTester;
import org.picketlink.test.idm.util.JPAContextInitializer;

import javax.persistence.EntityManager;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test for initialize file,jpa and ldap stores from XML configuration and perform some IDM operations with them
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@RunWith(Parameterized.class)
public class XMLConfigurationOperationsTestCase {

    private final IdentityConfigurationTester visitor;
    private PartitionManager partitionManager;

    public XMLConfigurationOperationsTestCase(IdentityConfigurationTester visitor) {
        this.visitor = visitor;
    }

    @Before
    public void onBefore() {
        this.visitor.beforeTest();
        this.partitionManager = this.visitor.getPartitionManager();
    }

    @After
    public void onAfter() {
        this.visitor.afterTest();
    }

    @Test
    public void testUser() {
        IdentityManager identityManager = partitionManager.createIdentityManager();
        assertNull(BasicModel.getUser(identityManager, "john"));
        identityManager.add(new User("john"));

        User john = BasicModel.getUser(identityManager, "john");
        assertNotNull(john);

        john.setFirstName("John");
        john.setLastName("Anthony");
        identityManager.update(john);

        john = BasicModel.getUser(identityManager, "john");
        assertNotNull(john);
        assertEquals(john.getFirstName(), "John");
        assertEquals(john.getLastName(), "Anthony");

        identityManager.remove(john);
        assertNull(BasicModel.getUser(identityManager, "john"));
    }

    @Test
    public void testGroupMemberships() {
        IdentityManager identityManager = partitionManager.createIdentityManager();
        identityManager.add(new User("mary"));

        // Add some groups
        identityManager.add(new Group("platform"));
        Group platform = BasicModel.getGroup(identityManager, "/platform");
        assertNotNull(platform);
        identityManager.add(new Group("users", platform));
        identityManager.add(new Group("administrators", platform));

        // assert that groups are here
        Group users = BasicModel.getGroup(identityManager, "/platform/users");
        Group administrators = BasicModel.getGroup(identityManager, "/platform/administrators");
        User mary = BasicModel.getUser(identityManager, "mary");
        assertNotNull(users);
        assertNotNull(administrators);
        assertNotNull(mary);
        assertEquals(users.getParentGroup().getName(), "platform");

        // Basic test of GroupMembership
        RelationshipManager relManager = partitionManager.createRelationshipManager();

        BasicModel.addToGroup(relManager, mary, users);
        assertTrue(BasicModel.isMember(relManager, mary, users));
        assertFalse(BasicModel.isMember(relManager, mary, administrators));

        BasicModel.removeFromGroup(relManager, mary, users);
        assertFalse(BasicModel.isMember(relManager, mary, users));
    }


    // STATIC METHODS (XML INITIALIZATION OF PARTICULAR TESTERS)

    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        List<Object[]> parameters = new ArrayList<Object[]>();

        for (IdentityConfigurationTester tester: getConfigurations()) {
            parameters.add(new Object[] {tester});
        }

        return parameters;
    }

    private static IdentityConfigurationTester[] getConfigurations() {
        return new IdentityConfigurationTester[] {
                new JPAXMLConfigTester(),
                new FileXMLConfigTester(),
                new LDAPXMLConfigTester()
        };
    }

    private static class JPAXMLConfigTester extends JPAStoreConfigurationTester {

        @Override
        public DefaultPartitionManager getPartitionManager() {
            IdentityConfigurationBuilder builder = readIDMConfiguration("config/embedded-jpa-config.xml");

            // Inject EntityManager programmatically
            builder
                .named(SIMPLE_JPA_STORE_CONFIG)
                    .stores()
                        .jpa()
                            .addContextInitializer(new JPAContextInitializer(null) {

                                @Override
                                public EntityManager getEntityManager() {
                                    return JPAXMLConfigTester.this.getEntityManager();
                                }

                            });

            DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
            return partitionManager;
        }

    }

    private static class FileXMLConfigTester extends FileStoreConfigurationTester {

        @Override
        public DefaultPartitionManager getPartitionManager() {
            IdentityConfigurationBuilder builder = readIDMConfiguration("config/embedded-file-config.xml");

            DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

            if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
                partitionManager.add(new Realm(Realm.DEFAULT_REALM));
            }

            return partitionManager;
        }

    }

    private static class LDAPXMLConfigTester extends LDAPStoreConfigurationTester {

        @Override
        public DefaultPartitionManager getPartitionManager() {
            IdentityConfigurationBuilder builder = readIDMConfiguration("config/embedded-ldap-config.xml");
            return new DefaultPartitionManager(builder.buildAll());
        }

    }

    private static IdentityConfigurationBuilder readIDMConfiguration(String configFilePath) {
        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream(configFilePath);
        XMLConfigurationProvider xmlConfigurationProvider = new XMLConfigurationProvider();
        return xmlConfigurationProvider.readIDMConfiguration(configStream);
    }

}
