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
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.MultipleIdentityConfigurationTester;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
@Configuration (include = MultipleIdentityConfigurationTester.class)
public class MultipleIdentityConfigurationTestCase extends AbstractPartitionManagerTestCase {

    public MultipleIdentityConfigurationTestCase(IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testUserManagement() {
        PartitionManager partitionManager = getPartitionManager();

        // let's create user Bob using JPA
        IdentityManager jpaIdentityManager = partitionManager.createIdentityManager();

        User bob = new User("bob");
        Password bobPassword = new Password("bob");

        jpaIdentityManager.add(bob);
        jpaIdentityManager.updateCredential(bob, bobPassword);

        // let's create a partition using the LDAP configuration and store user Mary
        Realm ldapRealm = new Realm("ldap.partition");

        partitionManager.add(ldapRealm, MultipleIdentityConfigurationTester.LDAP_CONFIGURATION_NAME);

        IdentityManager ldapIdentityManager = partitionManager.createIdentityManager(ldapRealm);

        User mary = new User("bob");
        Password maryPassword = new Password("ldapMary");

        ldapIdentityManager.add(mary);
        ldapIdentityManager.updateCredential(mary, maryPassword);

        // let's validate bob credentials
        UsernamePasswordCredentials jpaCredentials = new UsernamePasswordCredentials(bob.getLoginName(), bobPassword);

        jpaIdentityManager.validateCredentials(jpaCredentials);

        assertEquals(Credentials.Status.VALID, jpaCredentials.getStatus());

        // let's validate mary credentials
        UsernamePasswordCredentials ldapCredentials = new UsernamePasswordCredentials(mary.getLoginName(), maryPassword);

        ldapIdentityManager.validateCredentials(ldapCredentials);

        assertEquals(Credentials.Status.VALID, ldapCredentials.getStatus());

        // let's validate mary credentials using bob's password. this should fail.
        ldapIdentityManager.validateCredentials(jpaCredentials);

        assertEquals(Credentials.Status.INVALID, jpaCredentials.getStatus());

        // let's validate bob credentials using mary's password. this should fail.
        jpaIdentityManager.validateCredentials(ldapCredentials);

        assertEquals(Credentials.Status.INVALID, ldapCredentials.getStatus());
    }

    @Test
    public void testRoleManagement() {
        PartitionManager partitionManager = getPartitionManager();

        // let's create user Bob using JPA
        IdentityManager jpaIdentityManager = partitionManager.createIdentityManager();

        User bob = new User("bob");

        jpaIdentityManager.add(bob);

        // let's create a partition using the LDAP configuration and store user Mary
        Realm ldapRealm = new Realm("ldap.partition");

        partitionManager.add(ldapRealm, MultipleIdentityConfigurationTester.LDAP_CONFIGURATION_NAME);

        IdentityManager ldapIdentityManager = partitionManager.createIdentityManager(ldapRealm);

        User mary = new User("mary");

        ldapIdentityManager.add(mary);

        Role employee = new Role("employee");

        // roles are stores in JPA
        jpaIdentityManager.add(employee);

        RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

        // relationships are stores in JPA. we can reference types from different partitions.
        BasicModel.grantRole(relationshipManager, bob, employee);
        BasicModel.grantRole(relationshipManager, mary, employee);

        assertTrue(BasicModel.hasRole(relationshipManager, bob, employee));
        assertTrue(BasicModel.hasRole(relationshipManager, mary, employee));
    }
}
