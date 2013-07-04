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

import org.picketlink.idm.config.IdentityStoreConfigurationBuilder;

/**
 * @author Pedro Silva
 * 
 */
public abstract class AbstractFeaturesSetConfigurationTestCase<T extends IdentityStoreConfigurationBuilder<?, ?>> {

//    @Test
//    public void testMinimalConfigurationForIdentityIdentityOperations() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        IdentityManager identityManager = createIdentityManager(builder.build());
//
//        User user = new User("someUser");
//
//        performGetCreateRemoveIdentityType(user, identityManager);
//
//        Role role = new Role("someRole");
//
//        performGetCreateRemoveIdentityType(role, identityManager);
//
//        Group group = new Group("someGroup");
//
//        performGetCreateRemoveIdentityType(group, identityManager);
//    }
//
//    @Test
//    public void testMinimalConfigurationForRelationships() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        IdentityConfiguration configuration = builder.build();
//
//        PartitionManager partitionManager = createPartitionManager(configuration);
//        IdentityManager identityManager = createIdentityManager(configuration);
//
//        User user = identityManager.getUser("someUser");
//
//        if (user != null) {
//            identityManager.remove(user);
//        }
//
//        user = new User("someUser");
//
//        identityManager.add(user);
//
//        Role role = identityManager.getRole("someRole");
//
//        if (role != null) {
//            identityManager.remove(role);
//        }
//
//        role = new Role("someRole");
//
//        identityManager.add(role);
//
//        Group group = identityManager.getGroup("someGroup");
//
//        if (group != null) {
//            identityManager.remove(group);
//        }
//
//        group = new Group("someGroup");
//
//        identityManager.add(group);
//
//        partitionManager.grantRole(user, role);
//        partitionManager.grantGroupRole(user, role, group);
//        partitionManager.addToGroup(user, group);
//    }
//
//    @Test
//    public void testMinimalConfigurationForCredentials() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        IdentityManager identityManager = createIdentityManager(builder.build());
//
//        User user = identityManager.getUser("someUser");
//
//        if (user != null) {
//            identityManager.remove(user);
//        }
//
//        user = new User("someUser");
//
//        identityManager.add(user);
//
//        Password password = new Password("123");
//
//        identityManager.updateCredential(user, password);
//
//        identityManager.validateCredentials(new UsernamePasswordCredentials(user.getLoginName(), password));
//    }
//
//    @Test
//    public void failFeatureNotSupportedUserRead() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        storeConfig.unsupportType(User.class, IdentityOperation.read);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            identityManager.getUser("someUser");
//
//            fail();
//        } catch (OperationNotSupportedException one) {
//            assertTrue(one.getAttributedType().equals(User.class));
//            assertTrue(one.getOperation().equals(IdentityOperation.read));
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedUserCreate() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//
//        storeConfig.unsupportType(User.class, IdentityOperation.create);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            performGetCreateRemoveIdentityType(new User("someUser"), identityManager);
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//                assertTrue(one.getAttributedType().equals(User.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.create));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedUserDelete() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//
//        storeConfig.unsupportType(User.class, IdentityOperation.delete);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            User user = identityManager.getUser("someUser");
//
//            if (user == null) {
//                user = new User("someUser");
//                identityManager.add(user);
//            }
//
//            identityManager.remove(user);
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//                assertTrue(one.getAttributedType().equals(User.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.delete));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//
//    }
//
//    @Test
//    public void failFeatureNotSupportedRoleRead() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//
//        storeConfig.unsupportType(Role.class, IdentityOperation.read);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            identityManager.getRole("someRole");
//
//            fail();
//        } catch (OperationNotSupportedException one) {
//            assertTrue(one.getAttributedType().equals(Role.class));
//            assertTrue(one.getOperation().equals(IdentityOperation.read));
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedRoleCreate() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Role.class, IdentityOperation.create);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            performGetCreateRemoveIdentityType(new Role("someRole"), identityManager);
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//                assertTrue(one.getAttributedType().equals(Role.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.create));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedRoleDelete() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Role.class, IdentityOperation.delete);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            performGetCreateRemoveIdentityType(new Role("someRole"), identityManager);
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//                assertTrue(one.getAttributedType().equals(Role.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.delete));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedGroupRead() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Group.class, IdentityOperation.read);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            identityManager.getGroup("someGroup");
//
//            fail();
//        } catch (OperationNotSupportedException one) {
//            assertTrue(one.getAttributedType().equals(Group.class));
//            assertTrue(one.getOperation().equals(IdentityOperation.read));
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedGroupCreate() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Group.class, IdentityOperation.create);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            performGetCreateRemoveIdentityType(new Group("someGroup"), identityManager);
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//
//                assertTrue(one.getAttributedType().equals(Group.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.create));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//
//    }
//
//    @Test
//    public void failFeatureNotSupportedGroupDelete() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Group.class, IdentityOperation.delete);
//
//        try {
//            IdentityManager identityManager = createIdentityManager(builder.build());
//
//            Group group = new Group("someGroup");
//
//            performGetCreateRemoveIdentityType(group, identityManager);
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//
//                assertTrue(one.getAttributedType().equals(Group.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.delete));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//
//    }
//
//    @Test
//    public void failFeatureNotSupportedRelationshipRead() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Relationship.class, IdentityOperation.read);
//
//        try {
//            PartitionManager partitionManager = createPartitionManager(builder.build());
//
//            partitionManager.createRelationshipQuery(Relationship.class);
//
//            fail();
//        } catch (OperationNotSupportedException one) {
//            assertTrue(one.getAttributedType().equals(Relationship.class));
//            assertTrue(one.getOperation().equals(IdentityOperation.read));
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedRelationshipCreate() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Grant.class, IdentityOperation.create);
//
//        try {
//            IdentityConfiguration configuration = builder.build();
//
//            PartitionManager partitionManager = createPartitionManager(configuration);
//            IdentityManager identityManager = createIdentityManager(configuration);
//
//            User user = new User("someUser");
//
//            performGetCreateRemoveIdentityType(user, identityManager);
//
//            Role role = new Role("someRole");
//
//            performGetCreateRemoveIdentityType(role, identityManager);
//
//            partitionManager.add(new Grant(user, role));
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//
//                assertTrue(one.getAttributedType().equals(Grant.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.create));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    public void failFeatureNotSupportedCustomRelationship() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        IdentityConfiguration configuration = builder.build();
//
//        PartitionManager partitionManager = createPartitionManager(configuration);
//        IdentityManager identityManager = createIdentityManager(configuration);
//
//        User user = new User("someUser");
//
//        identityManager.add(user);
//
//        Role role = new Role("someRole");
//
//        identityManager.add(role);
//
//        CustomRelationship customRelationship = new CustomRelationship();
//
//        customRelationship.setIdentityTypeA(user);
//        customRelationship.setIdentityTypeB(role);
//
//        try {
//            partitionManager.add(customRelationship);
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (SecurityConfigurationException.class.isInstance(ime.getCause())) {
//                SecurityConfigurationException sce = (SecurityConfigurationException) ime.getCause();
//
//                assertTrue(sce.getMessage().contains(CustomRelationship.class.getName()));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//
//        builder = new IdentityConfigurationBuilder();
//
//        storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(CustomRelationship.class);
//
//        identityManager = createIdentityManager(builder.build());
//
//        try {
//            partitionManager.add(customRelationship);
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//
//                assertTrue(one.getAttributedType().equals(CustomRelationship.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.create));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedRelationshipDelete() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.unsupportType(Grant.class, IdentityOperation.delete);
//
//        try {
//            IdentityConfiguration configuration = builder.build();
//
//            PartitionManager partitionManager = createPartitionManager(configuration);
//            IdentityManager identityManager = createIdentityManager(configuration);
//
//            User user = new User("someUser");
//
//            identityManager.add(user);
//
//            Role role = new Role("someRole");
//
//            identityManager.add(role);
//
//            Grant grant = new Grant(user, role);
//
//            partitionManager.add(grant);
//            partitionManager.remove(grant);
//
//            fail();
//        } catch (IdentityManagementException ime) {
//            if (OperationNotSupportedException.class.isInstance(ime.getCause())) {
//                OperationNotSupportedException one = (OperationNotSupportedException) ime.getCause();
//
//                assertTrue(one.getAttributedType().equals(Grant.class));
//                assertTrue(one.getOperation().equals(IdentityOperation.delete));
//            } else {
//                fail();
//            }
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedCredentialUpdate() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.supportCredentials(false);
//
//        IdentityManager identityManager = createIdentityManager(builder.build());
//
//        User user = new User("someUser");
//
//        performGetCreateRemoveIdentityType(user, identityManager);
//
//        Password password = new Password("123");
//
//        try {
//            identityManager.updateCredential(user, password);
//            fail();
//        } catch (OperationNotSupportedException one) {
//            fail("Check exception.");
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void failFeatureNotSupportedCredentialValidate() {
//        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();
//
//        T storeConfig = createMinimalConfiguration(builder);
//
//        addContextInitializers(storeConfig);
//
//        builder.named("default").stores().readFrom(new IdentityStoresConfiguration(null, null));
//
//        storeConfig.supportCredentials(false);
//
//        IdentityManager identityManager = createIdentityManager(builder.build());
//
//        User user = new User("someUser");
//
//        User storedType = identityManager.getUser(user.getLoginName());
//
//        if (storedType != null) {
//            identityManager.remove(storedType);
//        }
//
//        identityManager.add(user);
//
//        Password password = new Password("123");
//
//        identityManager.updateCredential(user, password);
//
//        try {
//            identityManager.validateCredentials(new UsernamePasswordCredentials(user.getLoginName(), password));
//            fail();
//        } catch (OperationNotSupportedException one) {
//            fail("Check exception.");
//        } catch (Exception e) {
//            fail();
//        }
//    }
//
//    protected void addContextInitializers(T config) {
//    }
//
//    protected abstract T createMinimalConfiguration(IdentityConfigurationBuilder builder);
//
//    protected IdentityManager createIdentityManager(IdentityConfiguration config) {
//        fail("Create PartitionManager.");
////        return new DefaultPartitionManager(config).createIdentityManager();
//        return null;
//    }
//
//    protected PartitionManager createPartitionManager(IdentityConfiguration config) {
//        fail("Create PartitionManager.");
//        return null;
//    }
//
//    protected void performGetCreateRemoveIdentityType(IdentityType identityType, IdentityManager identityManager) {
//        IdentityType storedType = getIdentityType(identityType, identityManager);
//
//        if (storedType != null) {
//            identityManager.remove(storedType);
//        }
//
//        identityManager.add(identityType);
//
//        storedType = getIdentityType(identityType, identityManager);
//
//        assertNotNull(storedType);
//        assertNotNull(storedType.getId());
//
//        identityManager.remove(storedType);
//
//        storedType = getIdentityType(storedType, identityManager);
//
//        assertNull(storedType);
//    }
//
//    protected IdentityType getIdentityType(IdentityType identityType, IdentityManager identityManager) {
//        if (User.class.isInstance(identityType)) {
//            User user = (User) identityType;
//
//            identityType = identityManager.getUser(user.getLoginName());
//        } else if (Agent.class.isInstance(identityType)) {
//            Agent agent = (Agent) identityType;
//
//            identityType = identityManager.getAgent(agent.getLoginName());
//        } else if (Role.class.isInstance(identityType)) {
//            Role role = (Role) identityType;
//
//            identityType = identityManager.getRole(role.getName());
//        } else if (Group.class.isInstance(identityType)) {
//            Group group = (Group) identityType;
//
//            identityType = identityManager.getGroup(group.getName());
//        }
//
//        return identityType;
//    }
}
