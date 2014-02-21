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
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.model.sample.complex.CustomerUser;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.User;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.picketlink.test.idm.partition.CustomPartitionTestCase.CustomPartition;

/**
 * @author Pedro Igor
 */
public class CustomTypesTestCase {

    @Test
    public void testSupportForCustomPartitionType() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(CustomPartition.class)
                        .supportType(IdentityType.class)
                        .supportType(Relationship.class);

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        partitionManager.add(new CustomPartition("Custom Partition"));

        assertNotNull(partitionManager.getPartition(CustomPartition.class, "Custom Partition"));
    }

    @Test
    public void testSupportForCustomIdentityType() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(CustomerUser.class)
                        .supportType(Partition.class)
                        .supportType(Relationship.class);

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        CustomerUser jackfrost = new CustomerUser("jackfrost");

        identityManager.add(jackfrost);

        List<CustomerUser> result = identityManager.createIdentityQuery(CustomerUser.class).getResultList();

        assertEquals(1, result.size());

        jackfrost = result.get(0);

        assertEquals("jackfrost", jackfrost.getUserName());
    }

    @Test (expected = IdentityManagementException.class)
    public void failUnsupportedIdentityType() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("default")
                .stores()
                    .file()
                        .supportType(CustomerUser.class)
                        .supportType(Partition.class)
                        .supportType(Relationship.class);

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        partitionManager.add(new Realm(Realm.DEFAULT_REALM));

        IdentityManager identityManager = partitionManager.createIdentityManager();

        User jackfrost = new User("jackfrost");

        // User type is not supported
        identityManager.add(jackfrost);
    }

}
