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

import org.junit.Test;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.OperationNotSupportedException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.partition.CustomPartitionTestCase;

import static org.junit.Assert.assertEquals;
import static org.picketlink.test.idm.partition.CustomPartitionTestCase.CustomPartition;

/**
 * <p>
 * Test case for the Configuration API.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
public class ConfigurationTestCase {

    @Test (expected = SecurityConfigurationException.class)
    public void failNoIdentityStoreProvided() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.named("default").stores();

        builder.build();
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failNoSupportedTypeProvided() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder.named("default").stores().file();

        builder.build();
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failMultipleConfigurationWithBuildMethod() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportAllFeatures()
            .named("anotherName")
                .stores()
                    .file()
                        .supportAllFeatures();

        builder.build();
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failMultipleConfigurationWithPartitions() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(CustomPartition.class)
                    .jpa()
                        .supportType(Partition.class)
                        .mappedEntity(IdentityTypeEntity.class)
                        .supportAllFeatures();

        builder.buildAll();
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failMultipleConfigurationWithCredentialSupport() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(Partition.class)
                        .supportCredentials(true)
                    .jpa()
                        .mappedEntity(IdentityTypeEntity.class)
                        .supportCredentials(true)
                        .supportType(IdentityType.class);

        builder.buildAll();
    }

    @Test (expected = IdentityManagementException.class)
    public void failNoIdentityType() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(Partition.class);

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        identityManager.add(new User("someUser"));
    }

    @Test (expected = OperationNotSupportedException.class)
    public void failNoPartitionSupport() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(IdentityType.class);

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));
    }

    @Test (expected = IdentityManagementException.class)
    public void failNoCredentialSupport() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(Partition.class)
                        .supportType(IdentityType.class);

        PartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("someUser");

        identityManager.add(user);

        identityManager.updateCredential(user, new Password("abcd1234"));
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failMultipleIdentityStoreWithCredentialSupport() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportCredentials(true)
                        .supportType(Partition.class)
                        .supportType(IdentityType.class)
                    .jpa()
                        .supportCredentials(true)
                        .supportType(Relationship.class);

        new DefaultPartitionManager(builder.buildAll());
    }

    @Test
    public void testMultipleIdentityStoreWithValidCredentialSupport() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportCredentials(true)
                        .supportType(Partition.class)
                        .supportType(IdentityType.class)
                    .jpa()
                        .supportCredentials(false)
                        .supportType(Relationship.class);

        new DefaultPartitionManager(builder.buildAll());
    }

    @Test
    public void testMoreNamedCalls() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportAllFeatures()
            .named("default")
                .stores()
                    .file()
                        .supportAllFeatures();

        assertEquals(builder.buildAll().size(), 1);
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failDuplicatedSupportedType() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(User.class, Partition.class)
                        .supportCredentials(false)
                    .jpa()
                        .mappedEntity(IdentityTypeEntity.class)
                        .supportType(User.class);

        builder.buildAll();
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failInvalidSupportedTypeConfiguration() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(Role.class)
                        .supportCredentials(false)
                    .jpa()
                        .mappedEntity(IdentityTypeEntity.class)
                        .supportType(IdentityType.class);

        builder.buildAll();
    }

    @Test (expected = SecurityConfigurationException.class)
    public void failInvalidSupportedRelationshipTypeConfiguration() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(Relationship.class)
                        .supportCredentials(false)
                    .jpa()
                        .supportType(Grant.class)
                        .mappedEntity(IdentityTypeEntity.class);

        builder.buildAll();
    }

}
