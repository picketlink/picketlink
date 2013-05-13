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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoresConfiguration;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
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
public abstract class AbstractFeaturesSetConfigurationTestCase<T extends IdentityStoreConfigurationBuilder<?, ?>> {

    @Test
    public void testMinimalConfigurationForIdentityTypeOperations() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);

        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        IdentityManager identityManager = createIdentityManager(builder.build());

        User user = new SimpleUser("someUser");

        performGetCreateRemoveIdentityType(user, identityManager);

        Role role = new SimpleRole("someRole");

        performGetCreateRemoveIdentityType(role, identityManager);

        Group group = new SimpleGroup("someGroup");

        performGetCreateRemoveIdentityType(group, identityManager);
    }

    @Test
    public void testMinimalConfigurationForRelationships() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);

        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        IdentityManager identityManager = createIdentityManager(builder.build());

        User user = identityManager.getUser("someUser");

        if (user != null) {
            identityManager.remove(user);
        }

        user = new SimpleUser("someUser");

        identityManager.add(user);

        Role role = identityManager.getRole("someRole");

        if (role != null) {
            identityManager.remove(role);
        }

        role = new SimpleRole("someRole");

        identityManager.add(role);

        Group group = identityManager.getGroup("someGroup");

        if (group != null) {
            identityManager.remove(group);
        }

        group = new SimpleGroup("someGroup");

        identityManager.add(group);

        identityManager.grantRole(user, role);
        identityManager.grantGroupRole(user, role, group);
        identityManager.addToGroup(user, group);
    }

    @Test
    public void testMinimalConfigurationForCredentials() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);

        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        IdentityManager identityManager = createIdentityManager(builder.build());

        User user = identityManager.getUser("someUser");

        if (user != null) {
            identityManager.remove(user);
        }

        user = new SimpleUser("someUser");

        identityManager.add(user);

        Password password = new Password("123");

        identityManager.updateCredential(user, password);

        identityManager.validateCredentials(new UsernamePasswordCredentials(user.getLoginName(), password));
    }

    @Test
    public void failFeatureNotSupportedUserRead() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);
        
        storeConfig.removeFeature(FeatureGroup.user, FeatureOperation.read);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            identityManager.getUser("someUser");

            fail();
        } catch (OperationNotSupportedException one) {
            assertTrue(one.getFeatureGroup().equals(FeatureGroup.user));
            assertTrue(one.getFeatureOperation().equals(FeatureOperation.read));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedUserCreate() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));


        storeConfig.removeFeature(FeatureGroup.user, FeatureOperation.create);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            performGetCreateRemoveIdentityType(new SimpleUser("someUser"), identityManager);

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.user));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.create));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedUserDelete() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));


        storeConfig.removeFeature(FeatureGroup.user, FeatureOperation.delete);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            User user = identityManager.getUser("someUser");

            if (user == null) {
                user = new SimpleUser("someUser");
                identityManager.add(user);
            }

            identityManager.remove(user);

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.user));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.delete));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void failFeatureNotSupportedRoleRead() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));


        storeConfig.removeFeature(FeatureGroup.role, FeatureOperation.read);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            identityManager.getRole("someRole");

            fail();
        } catch (OperationNotSupportedException one) {
            assertTrue(one.getFeatureGroup().equals(FeatureGroup.role));
            assertTrue(one.getFeatureOperation().equals(FeatureOperation.read));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedRoleCreate() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.role, FeatureOperation.create);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            performGetCreateRemoveIdentityType(new SimpleRole("someRole"), identityManager);

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.role));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.create));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedRoleDelete() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.role, FeatureOperation.delete);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            performGetCreateRemoveIdentityType(new SimpleRole("someRole"), identityManager);

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.role));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.delete));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedGroupRead() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.group, FeatureOperation.read);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            identityManager.getGroup("someGroup");

            fail();
        } catch (OperationNotSupportedException one) {
            assertTrue(one.getFeatureGroup().equals(FeatureGroup.group));
            assertTrue(one.getFeatureOperation().equals(FeatureOperation.read));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedGroupCreate() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.group, FeatureOperation.create);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            performGetCreateRemoveIdentityType(new SimpleGroup("someGroup"), identityManager);

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.group));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.create));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void failFeatureNotSupportedGroupDelete() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.group, FeatureOperation.delete);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            SimpleGroup group = new SimpleGroup("someGroup");

            performGetCreateRemoveIdentityType(group, identityManager);

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.group));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.delete));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }

    }

    @Test
    public void failFeatureNotSupportedRelationshipRead() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.relationship, FeatureOperation.read);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            identityManager.createRelationshipQuery(Relationship.class);

            fail();
        } catch (OperationNotSupportedException one) {
            assertTrue(one.getFeatureGroup().equals(FeatureGroup.relationship));
            assertTrue(one.getFeatureOperation().equals(FeatureOperation.read));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedRelationshipCreate() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.relationship, FeatureOperation.create);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            User user = new SimpleUser("someUser");

            performGetCreateRemoveIdentityType(user, identityManager);

            Role role = new SimpleRole("someRole");

            performGetCreateRemoveIdentityType(role, identityManager);

            identityManager.add(new Grant(user, role));

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.relationship));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.create));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    public void failFeatureNotSupportedCustomRelationship() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        IdentityManager identityManager = createIdentityManager(builder.build());

        User user = new SimpleUser("someUser");

        identityManager.add(user);

        Role role = new SimpleRole("someRole");

        identityManager.add(role);

        CustomRelationship customRelationship = new CustomRelationship();

        customRelationship.setIdentityTypeA(user);
        customRelationship.setIdentityTypeB(role);

        try {
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

        builder = new IdentityConfigurationBuilder();
        
        storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeRelationship(CustomRelationship.class);

        identityManager = createIdentityManager(builder.build());

        try {
            identityManager.add(customRelationship);
            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.relationship));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.create));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedRelationshipDelete() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.relationship, FeatureOperation.delete);

        try {
            IdentityManager identityManager = createIdentityManager(builder.build());

            User user = new SimpleUser("someUser");

            identityManager.add(user);

            Role role = new SimpleRole("someRole");

            identityManager.add(role);

            Grant grant = new Grant(user, role);

            identityManager.add(grant);

            identityManager.remove(grant);

            fail();
        } catch (IdentityManagementException ime) {
            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();

                assertTrue(one.getFeatureGroup().equals(FeatureGroup.relationship));
                assertTrue(one.getFeatureOperation().equals(FeatureOperation.delete));
            } else {
                fail();
            }
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void failFeatureNotSupportedCredentialUpdate() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.credential, FeatureOperation.update);

        IdentityManager identityManager = createIdentityManager(builder.build());

        User user = new SimpleUser("someUser");

        performGetCreateRemoveIdentityType(user, identityManager);

        Password password = new Password("123");

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
    public void failFeatureNotSupportedCredentialValidate() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
        
        T storeConfig = createMinimalConfiguration(builder);

        addContextInitializers(storeConfig);
        
        builder.stores().readFrom(new IdentityStoresConfiguration((List<IdentityStoreConfiguration>) Arrays.asList(storeConfig.create()), null));

        storeConfig.removeFeature(FeatureGroup.credential, FeatureOperation.validate);

        IdentityManager identityManager = createIdentityManager(builder.build());

        User user = new SimpleUser("someUser");

        User storedType = identityManager.getUser(user.getLoginName());

        if (storedType != null) {
            identityManager.remove(storedType);
        }

        identityManager.add(user);

        Password password = new Password("123");

        identityManager.updateCredential(user, password);

        try {
            identityManager.validateCredentials(new UsernamePasswordCredentials(user.getLoginName(), password));
            fail();
        } catch (OperationNotSupportedException one) {
            assertTrue(one.getFeatureGroup().equals(FeatureGroup.credential));
            assertTrue(one.getFeatureOperation().equals(FeatureOperation.validate));
        } catch (Exception e) {
            fail();
        }
    }

    protected void addContextInitializers(T config) {
    }

    protected abstract T createMinimalConfiguration(IdentityConfigurationBuilder builder);

    protected abstract IdentityManager createIdentityManager(IdentityConfiguration config);

    protected void performGetCreateRemoveIdentityType(IdentityType identityType, IdentityManager identityManager) {
        IdentityType storedType = getIdentityType(identityType, identityManager);

        if (storedType != null) {
            identityManager.remove(storedType);
        }

        identityManager.add(identityType);

        storedType = getIdentityType(identityType, identityManager);

        assertNotNull(storedType);
        assertNotNull(storedType.getId());

        identityManager.remove(storedType);

        storedType = getIdentityType(storedType, identityManager);

        assertNull(storedType);
    }

    protected IdentityType getIdentityType(IdentityType identityType, IdentityManager identityManager) {
        if (User.class.isInstance(identityType)) {
            User user = (User) identityType;

            identityType = identityManager.getUser(user.getLoginName());
        } else if (Agent.class.isInstance(identityType)) {
            Agent agent = (Agent) identityType;

            identityType = identityManager.getAgent(agent.getLoginName());
        } else if (Role.class.isInstance(identityType)) {
            Role role = (Role) identityType;

            identityType = identityManager.getRole(role.getName());
        } else if (Group.class.isInstance(identityType)) {
            Group group = (Group) identityType;

            identityType = identityManager.getGroup(group.getName());
        }

        return identityType;
    }
}
