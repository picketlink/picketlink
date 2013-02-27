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

import org.junit.Assert;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleGroup;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.relationship.CustomRelationship;

/**
 * @author Pedro Silva
 *
 */
public abstract class AbstractFeaturesSetConfigurationTestCase<T extends IdentityStoreConfiguration> {

    @Test
    public void failFeatureNotSupportedUserRead() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.user, FeatureOperation.read);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            identityManager.getUser("someUser");

            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[user.read]"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedUserCreate() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.user, FeatureOperation.create);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            identityManager.add(new SimpleUser("someUser"));

            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[user.create]"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedUserDelete() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.user, FeatureOperation.delete);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            SimpleUser user = new SimpleUser("someUser");

            identityManager.add(user);
            identityManager.remove(user);

            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[user.delete]"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedRoleRead() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.role, FeatureOperation.read);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            identityManager.getRole("someRole");

            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[role.read]"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedRoleCreate() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.role, FeatureOperation.create);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            identityManager.add(new SimpleRole("someRole"));
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().toLowerCase().contains("[role.create]"));
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void failFeatureNotSupportedRoleDelete() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.role, FeatureOperation.delete);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            SimpleRole role = new SimpleRole("someRole");
            
            identityManager.add(role);
            
            identityManager.remove(role);
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().toLowerCase().contains("[role.delete]"));
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void failFeatureNotSupportedGroupRead() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.group, FeatureOperation.read);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            identityManager.getGroup("someGroup");

            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[group.read]"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedGroupCreate() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.group, FeatureOperation.create);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            identityManager.add(new SimpleGroup("someGroup"));
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().toLowerCase().contains("[group.create]"));
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void failFeatureNotSupportedGroupDelete() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.group, FeatureOperation.delete);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            SimpleGroup role = new SimpleGroup("someGroup");
            
            identityManager.add(role);
            
            identityManager.remove(role);
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().toLowerCase().contains("[group.delete]"));
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void failFeatureNotSupportedRelationshipRead() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.relationship, FeatureOperation.read);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);
            
            identityManager.createRelationshipQuery(Relationship.class);

            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[relationship.read]"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedRelationshipCreate() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.relationship, FeatureOperation.create);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            User user = new SimpleUser();

            identityManager.add(user);

            Role role = new SimpleRole("someRole");

            identityManager.add(role);
            
            identityManager.add(new Grant(user, role));
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().toLowerCase().contains("[relationship.create]"));
        } catch (Exception e) {
            Assert.fail();
        }
    }
    
    @Test
    public void failFeatureNotSupportedCustomRelationship() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().setSupportsCustomRelationships(false);

        config.addStoreConfiguration(storeConfig);

        IdentityManager identityManager = createIdentityManager(config);

        User user = new SimpleUser();

        identityManager.add(user);

        Role role = new SimpleRole("someRole");

        identityManager.add(role);
        
        CustomRelationship customRelationship = new CustomRelationship();
        
        customRelationship.setIdentityTypeA(user);
        customRelationship.setIdentityTypeB(role);

        try {
            identityManager.add(customRelationship);
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().contains("[" + CustomRelationship.class.getName() + "]"));
        } catch (Exception e) {
            Assert.fail();
        }
        
        config = new IdentityConfiguration();

        storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeRelationshipSupport(storeConfig.getFeatureSet(), CustomRelationship.class);

        config.addStoreConfiguration(storeConfig);

        identityManager = createIdentityManager(config);
        
        try {
            identityManager.add(customRelationship);
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().contains("[relationship.create]"));
        } catch (Exception e) {
            Assert.fail();
        }

    }
    
    @Test
    public void failFeatureNotSupportedRelationshipDelete() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.relationship, FeatureOperation.delete);

        config.addStoreConfiguration(storeConfig);

        try {
            IdentityManager identityManager = createIdentityManager(config);

            User user = new SimpleUser();

            identityManager.add(user);

            Role role = new SimpleRole("someRole");

            identityManager.add(role);
            
            Grant grant = new Grant(user, role);
            
            identityManager.add(grant);
            
            identityManager.remove(grant);
            
            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().toLowerCase().contains("[relationship.delete]"));
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Test
    public void failFeatureNotSupportedCredentialUpdate() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.credential, FeatureOperation.update);

        config.addStoreConfiguration(storeConfig);

        IdentityManager identityManager = createIdentityManager(config);

        User user = new SimpleUser();

        identityManager.add(user);

        Password password = new Password("123");

        try {
            identityManager.updateCredential(user, password);
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[credential.update]"));
        }
    }

    @Test
    public void failFeatureNotSupportedCredentialValidate() {
        IdentityConfiguration config = new IdentityConfiguration();

        T storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().removeFeature(FeatureGroup.credential, FeatureOperation.validate);

        config.addStoreConfiguration(storeConfig);

        IdentityManager identityManager = createIdentityManager(config);

        User user = new SimpleUser();

        identityManager.add(user);

        Password password = new Password("123");

        identityManager.updateCredential(user, password);
        
        try {
            identityManager.validateCredentials(new UsernamePasswordCredentials(user.getLoginName(), password));
            fail();
        } catch (SecurityConfigurationException sce) {
            assertTrue(sce.getMessage().toLowerCase().contains("[credential.validate]"));
        }
    }
    
    protected abstract T createMinimalConfiguration();
    
    protected abstract IdentityManager createIdentityManager(IdentityConfiguration config);}
