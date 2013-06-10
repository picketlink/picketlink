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
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.test.idm.suites.LDAPAbstractSuite;

/**
 * <p>Some tests for the {@link IdentityConfiguration} using basically the built-in stores configuration.</p>
 * 
 * @author Pedro Silva
 * 
 */
@Ignore
public class ProgrammaticConfigurationTestCase extends LDAPAbstractSuite {

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    @Before
    public void onInit() throws Exception {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();

        super.importLDIF("ldap/users.ldif");
    }

    @After
    public void onDestroy() throws Exception {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();

        super.tearDown();
    }

    @Test
    public void testFileIdentityStoreConfiguration() throws Exception {
//        IdentityConfiguration configuration = new IdentityConfiguration();
//
//        configuration
//            .fileStore()
//                .setAlwaysCreateFiles(true)
//                .setWorkingDir("/tmp/pl-idm")
//                .setAsyncWrite(true)
//                .setAsyncThreadPool(10)
//                .addRealm(Realm.DEFAULT_REALM)
//                .addRealm("Realm")
//                .addTier("Tier")
//                .supportAllFeatures(); // you can also enable features individually. eg.:supportFeature(FeatureGroup.user)
//
//        IdentityManagerFactory identityManagerFactory = configuration.buildIdentityManagerFactory();
//
//        IdentityManager identityManager = identityManagerFactory.createIdentityManager();
//
//        User user = new SimpleUser("user");
//
//        identityManager.add(user);
//
//        assertNotNull(identityManager.getUser(user.getLoginName()));
    }

    @Test
    public void testJPAIdentityStoreConfiguration() throws Exception {
//        IdentityConfiguration configuration = new IdentityConfiguration();
//
//        configuration
//            .jpaStore()
//                .addContextInitializer(new JPAContextInitializer(emf) {
//                    @Override
//                    public EntityManager getEntityManager() {
//                        return entityManager;
//                    }
//                })
//                .setIdentityClass(IdentityObject.class)
//                .setAttributeClass(IdentityObjectAttribute.class)
//                .setRelationshipClass(RelationshipObject.class)
//                .setRelationshipIdentityClass(RelationshipIdentityObject.class)
//                .setRelationshipAttributeClass(RelationshipObjectAttribute.class)
//                .setCredentialClass(CredentialObject.class)
//                .setCredentialAttributeClass(CredentialObjectAttribute.class)
//                .setPartitionClass(PartitionObject.class)
//                .addRealm(Realm.DEFAULT_REALM)
//                .addRealm("Realm")
//                .addTier("Tier")
//                .supportAllFeatures(); // you can also enable features individually. eg.: supportFeature(FeatureGroup.user)
//
//        IdentityManagerFactory identityManagerFactory = configuration.buildIdentityManagerFactory();
//
//        IdentityManager identityManager = identityManagerFactory.createIdentityManager();
//
//        User user = new SimpleUser("user");
//
//        identityManager.add(user);
//
//        assertNotNull(identityManager.getUser(user.getLoginName()));
    }
    
    @Test
    public void testLDAPIdentityStoreConfiguration() throws Exception {
//        IdentityConfiguration configuration = new IdentityConfiguration();
//
//        configuration
//            .ldapStore()
//                .setBaseDN(BASE_DN)
//                .setBindDN("uid=admin,ou=system")
//                .setBindCredential("secret")
//                .setLdapURL(LDAP_URL)
//                .setUserDNSuffix(USER_DN_SUFFIX)
//                .setRoleDNSuffix(ROLES_DN_SUFFIX)
//                .setAgentDNSuffix(AGENT_DN_SUFFIX)
//                .setGroupDNSuffix(GROUP_DN_SUFFIX)
//                .addRealm(Realm.DEFAULT_REALM)
//                .supportFeature(
//                        FeatureGroup.user, 
//                        FeatureGroup.agent, 
//                        FeatureGroup.user, 
//                        FeatureGroup.group,
//                        FeatureGroup.role, 
//                        FeatureGroup.attribute, 
//                        FeatureGroup.relationship, 
//                        FeatureGroup.credential);
//
//        IdentityManagerFactory identityManagerFactory = configuration.buildIdentityManagerFactory();
//
//        IdentityManager identityManager = identityManagerFactory.createIdentityManager();
//
//        User user = new SimpleUser("user");
//
//        identityManager.add(user);
//
//        assertNotNull(identityManager.getUser(user.getLoginName()));
    }
    
    @Test
    @SuppressWarnings ("unchecked")
    public void testLDAPAndJPAIdentityStoreConfiguration() throws Exception {
//        IdentityConfiguration configuration = new IdentityConfiguration();
//
//        configuration
//            .ldapStore()
//                .setBaseDN(BASE_DN)
//                .setBindDN("uid=admin,ou=system")
//                .setBindCredential("secret")
//                .setLdapURL(LDAP_URL)
//                .setUserDNSuffix(USER_DN_SUFFIX)
//                .setRoleDNSuffix(ROLES_DN_SUFFIX)
//                .setAgentDNSuffix(AGENT_DN_SUFFIX)
//                .setGroupDNSuffix(GROUP_DN_SUFFIX)
//                .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
//                .addRealm(Realm.DEFAULT_REALM)
//                .supportFeature(
//                    FeatureGroup.user, 
//                    FeatureGroup.agent, 
//                    FeatureGroup.user, 
//                    FeatureGroup.group,
//                    FeatureGroup.role, 
//                    FeatureGroup.attribute, 
//                    FeatureGroup.credential);
//        
//        configuration
//            .jpaStore()
//                .addRealm(Realm.DEFAULT_REALM)
//                .setIdentityClass(IdentityObject.class)
//                .setAttributeClass(IdentityObjectAttribute.class)
//                .setRelationshipClass(RelationshipObject.class)
//                .setRelationshipIdentityClass(RelationshipIdentityWeakObject.class)
//                .setRelationshipAttributeClass(RelationshipObjectAttribute.class)
//                .setPartitionClass(PartitionObject.class)
//                .supportFeature(FeatureGroup.relationship)
//                .supportRelationshipType(CustomRelationship.class, Authorization.class)
//                .addContextInitializer(new JPAContextInitializer(emf) {
//                    @Override
//                    public EntityManager getEntityManager() {
//                        return entityManager;
//                    }
//                });
//
//        IdentityManagerFactory identityManagerFactory = configuration.buildIdentityManagerFactory();
//
//        IdentityManager identityManager = identityManagerFactory.createIdentityManager();
//
//        User user = new SimpleUser("user");
//
//        identityManager.add(user);
//        
//        Role role = new SimpleRole("role");
//        
//        identityManager.add(role);
//        
//        identityManager.grantRole(user, role);
//
//        assertNotNull(identityManager.getUser(user.getLoginName()));
//        assertTrue(identityManager.hasRole(user, role));
    }

    @Test
    public void failDuplicatedFeatureConfiguration() throws Exception {
//        IdentityConfiguration configuration = new IdentityConfiguration();
//
//        configuration
//            .fileStore()
//                .supportFeature(FeatureGroup.user);
//        configuration
//            .jpaStore()
//                .supportFeature(FeatureGroup.user);
//
//        try {
//            configuration.buildIdentityManagerFactory();
//            fail();
//        } catch (SecurityConfigurationException e) {
//            assertTrue(e.getMessage().contains("PLIDM000069"));
//            
//            if (!e.getCause().getMessage().contains("PLIDM000071")) {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
    }
}
