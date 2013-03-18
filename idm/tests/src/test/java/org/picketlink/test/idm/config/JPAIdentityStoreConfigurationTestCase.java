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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.IdentityManagerFactory;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityManagerFactory;
import org.picketlink.idm.internal.DefaultSecurityContextFactory;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.relationship.CustomRelationship;

/**
 * <p>
 * Test case for the {@link JPAIdentityStoreConfiguration}.
 * </p>
 *  * @author Pedro Silva
 * 
 */
public class JPAIdentityStoreConfigurationTestCase extends
        AbstractFeaturesSetConfigurationTestCase<JPAIdentityStoreConfiguration> {

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    @Before
    public void onInit() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @After
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }

    @Test
    public void failFeatureNotSupportedWhenEntityClassesNotProvided() {
        IdentityConfiguration config = new IdentityConfiguration();

        JPAIdentityStoreConfiguration jpaConfig = createMinimalConfiguration();

        jpaConfig.setRelationshipClass(null);
        jpaConfig.setRelationshipIdentityClass(null);
        jpaConfig.setRelationshipAttributeClass(null);

        config.addStoreConfiguration(jpaConfig);

        IdentityManager identityManager = createIdentityManager(config);

        User user = new SimpleUser("someUser");

        identityManager.add(user);

        Role role = new SimpleRole("someRole");

        identityManager.add(role);

        Group group = new SimpleGroup("someGroup");

        identityManager.add(group);

        try {
            identityManager.add(new Grant(user, role));
            fail();
        } catch (IdentityManagementException ime) {
            if (SecurityConfigurationException.class.isInstance(ime.getCause())) {
                SecurityConfigurationException sce = (SecurityConfigurationException) ime.getCause();
                
                assertTrue(sce.getMessage().contains(Grant.class.getName()));    
            } else {
                fail();
            }
        }

        try {
            identityManager.add(new GroupRole(user, group, role));
            fail();
        } catch (IdentityManagementException ime) {
            if (SecurityConfigurationException.class.isInstance(ime.getCause())) {
                SecurityConfigurationException sce = (SecurityConfigurationException) ime.getCause();
                
                assertTrue(sce.getMessage().contains(GroupRole.class.getName()));    
            } else {
                fail();
            }
        }

        try {
            identityManager.add(new GroupMembership(user, group));
            fail();
        } catch (IdentityManagementException ime) {
            if (SecurityConfigurationException.class.isInstance(ime.getCause())) {
                SecurityConfigurationException sce = (SecurityConfigurationException) ime.getCause();
                
                assertTrue(sce.getMessage().contains(GroupMembership.class.getName()));    
            } else {
                fail();
            }
        }

        try {
            CustomRelationship customRelationship = new CustomRelationship();

            customRelationship.setIdentityTypeA(user);
            customRelationship.setIdentityTypeB(role);
            customRelationship.setIdentityTypeC(group);

            identityManager.add(customRelationship);
            fail();
        } catch (IdentityManagementException ime) {
            if (SecurityConfigurationException.class.isInstance(ime.getCause())) {
                SecurityConfigurationException sce = (SecurityConfigurationException) ime.getCause();
                
                assertTrue(sce.getMessage().contains(CustomRelationship.class.getName()));    
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedWhenCredentialClassesNotProvided() {
        IdentityConfiguration config = new IdentityConfiguration();

        JPAIdentityStoreConfiguration jpaConfig = createMinimalConfiguration();

        jpaConfig.setCredentialClass(null);

        config.addStoreConfiguration(jpaConfig);

        IdentityManager identityManager = createIdentityManager(config);

        User user = new SimpleUser("someUser");

        identityManager.add(user);

        Password password = new Password("123");

        try {
            identityManager.updateCredential(user, password);
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[credential.update]"));
        }

        config = new IdentityConfiguration();

        jpaConfig = createMinimalConfiguration();

        jpaConfig.setCredentialClass(CredentialObject.class);
        jpaConfig.setCredentialAttributeClass(null);

        config.addStoreConfiguration(jpaConfig);

        identityManager = createIdentityManager(config);

        try {
            identityManager.updateCredential(user, password);
            fail();
        } catch (OperationNotSupportedException one) {
            assertTrue(one.getFeatureGroup().equals(FeatureGroup.credential));
            assertTrue(one.getFeatureOperation().equals(FeatureOperation.update));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failIdentityClassNotProvided() {
        IdentityConfiguration config = new IdentityConfiguration();

        config.addStoreConfiguration(new JPAIdentityStoreConfiguration());

        try {
            createIdentityManager(config);
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("identityclass not set"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failPartitionClassNotProvided() {
        IdentityConfiguration config = new IdentityConfiguration();
        config.addContextInitializer(new JPAContextInitializer(emf));

        JPAIdentityStoreConfiguration jpaConfig = new JPAIdentityStoreConfiguration();

        jpaConfig.setIdentityClass(IdentityObject.class);

        IdentityManagerFactory factory = new DefaultIdentityManagerFactory(config);
        IdentityManager identityManager = factory.createIdentityManager();

        config.addStoreConfiguration(jpaConfig);

        try {
            createIdentityManager(config);
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("partitionclass not set"));
        } catch (Exception e) {
            fail();
        }
    }

    @Override
    protected JPAIdentityStoreConfiguration createMinimalConfiguration() {
        JPAIdentityStoreConfiguration jpaConfig = new JPAIdentityStoreConfiguration();

        // mandatory entity classes
        jpaConfig.setIdentityClass(IdentityObject.class);
        jpaConfig.setPartitionClass(PartitionObject.class);

        // optional relationship entity classes
        jpaConfig.setRelationshipClass(RelationshipObject.class);
        jpaConfig.setRelationshipIdentityClass(RelationshipIdentityObject.class);
        jpaConfig.setRelationshipAttributeClass(RelationshipObjectAttribute.class);

        // optional credential entity classes
        jpaConfig.setCredentialClass(CredentialObject.class);
        jpaConfig.setCredentialAttributeClass(CredentialObjectAttribute.class);

        // enabled basic features for identitytypes
        FeatureSet.addFeatureSupport(jpaConfig.getFeatureSet(), FeatureGroup.user);
        FeatureSet.addFeatureSupport(jpaConfig.getFeatureSet(), FeatureGroup.role);
        FeatureSet.addFeatureSupport(jpaConfig.getFeatureSet(), FeatureGroup.group);
        
        // enable relationship features. this enables the default/built-in relationship classes
        FeatureSet.addFeatureSupport(jpaConfig.getFeatureSet(), FeatureGroup.relationship);
        
        // to enable custom relationship classes we need to set this flag.
        jpaConfig.getFeatureSet().setSupportsCustomRelationships(true);
        
        // enable the custom relationship class
        FeatureSet.addRelationshipSupport(jpaConfig.getFeatureSet(), CustomRelationship.class);
        
        // enable credentials
        FeatureSet.addFeatureSupport(jpaConfig.getFeatureSet(), FeatureGroup.credential);

        return jpaConfig;
    }

    @Override
    protected IdentityManager createIdentityManager(IdentityConfiguration config) {
        IdentityManagerFactory factory = new DefaultIdentityManagerFactory(config);
        return factory.createIdentityManager();
    }

}
