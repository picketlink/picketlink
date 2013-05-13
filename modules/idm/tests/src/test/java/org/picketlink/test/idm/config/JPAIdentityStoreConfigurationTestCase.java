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
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.config.JPAStoreConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.IdentityManagerFactory;
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
 * * @author Pedro Silva
 * 
 */
public class JPAIdentityStoreConfigurationTestCase extends
        AbstractFeaturesSetConfigurationTestCase<JPAStoreConfigurationBuilder> {

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
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        JPAStoreConfigurationBuilder jpaConfig = createMinimalConfiguration(builder);

        jpaConfig.relationshipClass(null);
        jpaConfig.relationshipIdentityClass(null);
        jpaConfig.relationshipAttributeClass(null);

        addContextInitializers(jpaConfig);

        IdentityManager identityManager = createIdentityManager(builder.build());

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
        // IdentityConfiguration config = new IdentityConfiguration();
        //
        // JPAIdentityStoreConfiguration jpaConfig = createMinimalConfiguration();
        //
        // jpaConfig.setCredentialClass(null);
        //
        // addContextInitializers(jpaConfig);
        // config.addConfig(jpaConfig);
        //
        // IdentityManager identityManager = createIdentityManager(config);
        //
        // User user = new SimpleUser("someUser");
        //
        // identityManager.add(user);
        //
        // Password password = new Password("123");
        //
        // try {
        // identityManager.updateCredential(user, password);
        // fail();
        // } catch (SecurityConfigurationException sce) {
        // assertTrue(sce.getMessage().toLowerCase().contains("[credential.update]"));
        // }
        //
        // config = new IdentityConfiguration();
        //
        // jpaConfig = createMinimalConfiguration();
        //
        // jpaConfig.setCredentialClass(CredentialObject.class);
        // jpaConfig.setCredentialAttributeClass(null);
        //
        // addContextInitializers(jpaConfig);
        // config.addConfig(jpaConfig);
        //
        // identityManager = createIdentityManager(config);
        //
        // try {
        // identityManager.updateCredential(user, password);
        // fail();
        // } catch (OperationNotSupportedException one) {
        // assertTrue(one.getFeatureGroup().equals(FeatureGroup.credential));
        // assertTrue(one.getFeatureOperation().equals(FeatureOperation.update));
        // } catch (Exception e) {
        // fail();
        // }
    }

    @Test
    public void failIdentityClassNotProvided() {
        // IdentityConfiguration config = new IdentityConfiguration();
        //
        // JPAIdentityStoreConfiguration jpaConfig = new JPAIdentityStoreConfiguration();
        //
        // addContextInitializers(jpaConfig);
        // config.addConfig(jpaConfig);
        //
        // try {
        // createIdentityManager(config);
        // fail();
        // } catch (SecurityConfigurationException sce) {
        // assertTrue(sce.getMessage().toLowerCase().contains("identityclass not set"));
        // } catch (Exception e) {
        // fail();
        // }
    }

    @Test
    public void failPartitionClassNotProvided() {
        // IdentityConfiguration config = new IdentityConfiguration();
        //
        // JPAIdentityStoreConfiguration jpaConfig = new JPAIdentityStoreConfiguration();
        //
        // jpaConfig.setIdentityClass(IdentityObject.class);
        //
        // addContextInitializers(jpaConfig);
        // config.addConfig(jpaConfig);
        //
        // try {
        // createIdentityManager(config);
        // fail();
        // } catch (SecurityConfigurationException sce) {
        // assertTrue(sce.getMessage().toLowerCase().contains("partitionclass not set"));
        // } catch (Exception e) {
        // fail();
        // }
    }

    @Override
    protected JPAStoreConfigurationBuilder createMinimalConfiguration(IdentityConfigurationBuilder builder) {
        JPAStoreConfigurationBuilder jpaConfig = builder.stores().jpa();

        // mandatory entity classes
        jpaConfig.identityClass(IdentityObject.class);
        jpaConfig.partitionClass(PartitionObject.class);

        // optional relationship entity classes
        jpaConfig.relationshipClass(RelationshipObject.class);
        jpaConfig.relationshipIdentityClass(RelationshipIdentityObject.class);
        jpaConfig.relationshipAttributeClass(RelationshipObjectAttribute.class);

        // optional credential entity classes
        jpaConfig.credentialClass(CredentialObject.class);
        jpaConfig.credentialAttributeClass(CredentialObjectAttribute.class);

        // enabled basic features
        jpaConfig.supportFeature(FeatureGroup.user, FeatureGroup.role, FeatureGroup.group, FeatureGroup.relationship, FeatureGroup.credential);

        // enable the custom relationship class
        jpaConfig.supportRelationshipType(CustomRelationship.class);

        return jpaConfig;
    }

    @Override
    protected IdentityManager createIdentityManager(IdentityConfiguration config) {
        IdentityManagerFactory factory = new IdentityManagerFactory(config);
        return factory.createIdentityManager();
    }

    @Override
    protected void addContextInitializers(JPAStoreConfigurationBuilder config) {
        config.addContextInitializer(new JPAContextInitializer(emf) {
            @Override
            public EntityManager getEntityManager() {
                return entityManager;
            }
        });
    }

}
