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
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * <p>Some tests for the {@link IdentityConfiguration}.</p>
 * 
 * @author Pedro Silva
 * 
 */
public class ProgrammaticConfigurationTestCase {

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
    }

    @Test
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
        }
    }

}
