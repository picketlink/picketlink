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

package org.picketlink.test.idm.config;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.suites.LDAPAbstractSuite;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>Some tests for the {@link IdentityConfiguration}.</p>
 * 
 * @author Pedro Silva
 * 
 */
public class ProgrammaticConfigurationTestCase {

    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private LDAPAbstractSuite ldapServer;

    @Before
    public void onInit() throws Exception {
//        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
//        this.entityManager = emf.createEntityManager();
//        this.entityManager.getTransaction().begin();
//        this.ldapServer = new LDAPAbstractSuite() {};
//
//        this.ldapServer.setup();
//        this.ldapServer.importLDIF("ldap/users.ldif");
    }

    @After
    public void onDestroy() throws Exception {
//        this.entityManager.getTransaction().commit();
//        this.entityManager.close();
//        this.emf.close();
//        this.ldapServer.tearDown();
    }

    @Test
    public void testFileIdentityStoreConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .preserveState(false)
                        .workingDirectory("/tmp/pl-idm")
                        .asyncWrite(true)
                        .asyncWriteThreadPool(10)
                        .supportAllFeatures(); // you can also enable features individually. eg.:supportFeature(FeatureGroup.user)

        PartitionManager partitionManager = new DefaultPartitionManager(builder.build());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM), "default");

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        assertNotNull(identityManager.getUser(user.getLoginName()));
    }

    @Test
    public void testJPAIdentityStoreConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                .jpa()
                .addContextInitializer(new JPAContextInitializer(emf) {
                    @Override
                    public EntityManager getEntityManager() {
                        return entityManager;
                    }
                })
                .identityClass(IdentityObject.class)
                .attributeClass(IdentityObjectAttribute.class)
                .relationshipClass(RelationshipObject.class)
                .relationshipIdentityClass(RelationshipIdentityObject.class)
                .relationshipAttributeClass(RelationshipObjectAttribute.class)
                .credentialClass(CredentialObject.class)
                .credentialAttributeClass(CredentialObjectAttribute.class)
                .partitionClass(PartitionObject.class)
                .supportAllFeatures(); // you can also enable features individually. eg.: supportFeature(FeatureGroup.user)

        PartitionManager partitionManager = null;
        fail("Create PartitionManager");

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        assertNotNull(identityManager.getUser(user.getLoginName()));
    }

    @Test
    public void testLDAPIdentityStoreConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                .ldap()
                .baseDN(this.ldapServer.getBaseDn())
                .bindDN(this.ldapServer.getBindDn())
                .bindCredential(this.ldapServer.getBindCredential())
                .url(this.ldapServer.getConnectionUrl())
                .userDNSuffix(this.ldapServer.getUserDnSuffix())
                .roleDNSuffix(this.ldapServer.getRolesDnSuffix())
                .agentDNSuffix(this.ldapServer.getAgentDnSuffix())
                .groupDNSuffix(this.ldapServer.getGroupDnSuffix())
                .supportAllFeatures();

        PartitionManager partitionManager = null;
        fail("Create PartitionManager");

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("user");

        identityManager.add(user);

        assertNotNull(identityManager.getUser(user.getLoginName()));
    }

    @Test
    public void failNoPartitionConfigurationProvided() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(User.class);

        try {
            PartitionManager partitionManager = new DefaultPartitionManager(builder.build());
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000074"));
        }
    }

    @Test
    public void failDuplicatedIdentityTypeConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
            .stores()
                .file()
                    .supportType(User.class)
                    .supportType(Partition.class)
                .file()
                    .supportType(User.class);

        try {
            PartitionManager partitionManager = new DefaultPartitionManager(builder.build());
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000074"));
        }
    }

    @Test
    public void failDuplicatedFeatureConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                .file()
                    .supportCredentials(true)
                .jpa()
                    .identityClass(IdentityObject.class)
                    .partitionClass(PartitionObject.class)
                    .supportCredentials(true);

        try {
            PartitionManager partitionManager = null;
            fail("Create PartitionManager");
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000071"));
        }
    }

    @Test
    public void failDuplicatedRelationshipConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                .file()
                    .supportType(Grant.class)
                .jpa()
                    .identityClass(IdentityObject.class)
                    .partitionClass(PartitionObject.class)
                    .supportType(Grant.class);

        try {
            PartitionManager partitionManager = null;
            fail("Create PartitionManager");
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000075"));
        }
    }

    @Test
    public void failUnsupportedIdentityType() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                    .file()
                        .supportType(Role.class);

        try {
            PartitionManager partitionManager = null;
            fail("Create PartitionManager");

            IdentityManager identityManager = partitionManager.createIdentityManager();

            identityManager.add(new User("john"));
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000076"));
        }
    }

    @Test
    public void failUnsupportedRelationshipType() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
                .named("default")
                .stores()
                    .file()
                        .supportType(IdentityType.class)
                        .supportType(GroupRole.class);

        try {
            PartitionManager partitionManager = null;
            fail("Create PartitionManager");

            IdentityManager identityManager = partitionManager.createIdentityManager();

            User john = new User("john");

            identityManager.add(john);

            Role manager = new Role("manager");

            identityManager.add(manager);

            partitionManager.createRelationshipManager().grantRole(john, manager);
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000016"));
        }
    }

}
