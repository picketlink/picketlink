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

<<<<<<< HEAD
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet;
=======
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.IdentityManagerFactory;
<<<<<<< HEAD
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
=======
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.suites.LDAPAbstractSuite;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310

/**
 * <p>Some tests for the {@link IdentityConfiguration}.</p>
 * 
 * @author Pedro Silva
 * 
 */
public class ProgrammaticConfigurationTestCase {

<<<<<<< HEAD
    @Test
    public void failDuplicatedIdentityTypeConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.
            stores()
                .file()
                    .supportIdentityType(User.class)
                .jpa()
                    .identityClass(IdentityObject.class)
                    .partitionClass(PartitionObject.class)
                    .supportIdentityType(User.class);

        try {
            new IdentityManagerFactory(builder.build());
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000074"));
        }
    }

    @Test
    public void failDuplicatedFeatureConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.
                stores()
                .file()
                    .supportFeature(FeatureSet.FeatureGroup.credential)
                .jpa()
                    .identityClass(IdentityObject.class)
                    .partitionClass(PartitionObject.class)
                    .supportFeature(FeatureSet.FeatureGroup.credential);

        try {
            new IdentityManagerFactory(builder.build());
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000071"));
        }
    }

    @Test
    public void failDuplicatedRelationshipConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.
                stores()
                .file()
                    .supportRelationshipType(Grant.class)
                .jpa()
                    .identityClass(IdentityObject.class)
                    .partitionClass(PartitionObject.class)
                    .supportRelationshipType(Grant.class);

        try {
            new IdentityManagerFactory(builder.build());
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000075"));
        }
=======
    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private LDAPAbstractSuite ldapServer;

    @Before
    public void onInit() throws Exception {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
        this.ldapServer = new LDAPAbstractSuite() {};

        this.ldapServer.setup();
        this.ldapServer.importLDIF("ldap/users.ldif");
    }

    @After
    public void onDestroy() throws Exception {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
        this.ldapServer.tearDown();
    }

    @Test
    public void testFileIdentityStoreConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .stores()
                .file()
                    .preserveState(false)
                    .workingDirectory("/tmp/pl-idm")
                    .asyncWrite(true)
                    .asyncWriteThreadPool(10)
                    .addRealm(Realm.DEFAULT_REALM)
                    .addRealm("Realm")
                    .addTier("Tier")
                    .supportAllFeatures(); // you can also enable features individually. eg.:supportFeature(FeatureGroup.user)

        IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(builder.build());

        IdentityManager identityManager = identityManagerFactory.createIdentityManager();

        User user = new SimpleUser("user");

        identityManager.add(user);

        assertNotNull(identityManager.getUser(user.getLoginName()));
    }

    @Test
    public void testJPAIdentityStoreConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
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
                    .addRealm(Realm.DEFAULT_REALM)
                    .addRealm("Realm")
                    .addTier("Tier")
                    .supportAllFeatures(); // you can also enable features individually. eg.: supportFeature(FeatureGroup.user)

        IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(builder.build());

        IdentityManager identityManager = identityManagerFactory.createIdentityManager();

        User user = new SimpleUser("user");

        identityManager.add(user);

        assertNotNull(identityManager.getUser(user.getLoginName()));
    }
    
    @Test
    public void testLDAPIdentityStoreConfiguration() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
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
                    .addRealm(Realm.DEFAULT_REALM)
                    .supportFeature(
                            FeatureGroup.user,
                            FeatureGroup.agent,
                            FeatureGroup.user,
                            FeatureGroup.group,
                            FeatureGroup.role,
                            FeatureGroup.attribute,
                            FeatureGroup.relationship,
                            FeatureGroup.credential);

        IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(builder.build());

        IdentityManager identityManager = identityManagerFactory.createIdentityManager();

        User user = new SimpleUser("user");

        identityManager.add(user);

        assertNotNull(identityManager.getUser(user.getLoginName()));
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
    }

    @Test
<<<<<<< HEAD
    public void failUnsupportedIdentityType() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.
                stores()
                    .file()
                        .supportIdentityType(Role.class);

        try {
            IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(builder.build());
            IdentityManager identityManager = identityManagerFactory.createIdentityManager();

            identityManager.add(new User("john"));
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000076"));
        }
    }

    @Test
    public void failUnsupportedRelationshipType() throws Exception {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.
                stores()
                    .file()
                        .supportFeature(FeatureSet.FeatureGroup.identity_type)
                        .supportRelationshipType(GroupRole.class);

        try {
            IdentityManagerFactory identityManagerFactory = new IdentityManagerFactory(builder.build());
            IdentityManager identityManager = identityManagerFactory.createIdentityManager();

            User john = new User("john");

            identityManager.add(john);

            Role manager = new Role("manager");

            identityManager.add(manager);

            identityManager.grantRole(john, manager);
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().contains("PLIDM000016"));
=======
    public void failDuplicatedFeatureConfiguration() throws Exception {
        IdentityConfigurationBuilder configuration = new IdentityConfigurationBuilder();

        configuration
            .stores()
                .file()
                    .supportFeature(FeatureGroup.user);

        configuration
            .stores()
                .jpa()
                    .identityClass(IdentityObject.class)
                    .partitionClass(PartitionObject.class)
                    .supportFeature(FeatureGroup.user);

        try {
            new IdentityManagerFactory(configuration.build());
            fail();
        } catch (SecurityConfigurationException e) {
            assertTrue(e.getMessage().contains("PLIDM000071"));

            if (!e.getMessage().contains("PLIDM000071")) {
                fail();
            }
        } catch (Exception e) {
            fail();
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
        }
    }

}
