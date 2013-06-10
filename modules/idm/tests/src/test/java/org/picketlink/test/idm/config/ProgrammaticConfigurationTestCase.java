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
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.IdentityManagerFactory;
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

/**
 * <p>Some tests for the {@link IdentityConfiguration} using basically the built-in stores configuration.</p>
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
    }
    
    @Test
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
        }
    }
}
