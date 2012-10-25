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
package org.picketlink.test.idm.internal.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPUser;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.spi.IdentityStoreConfigurationBuilder;

/**
 * Unit test the {@link LDAPIdentityStore}
 *
 * @author anil saldhana
 * @since Aug 30, 2012
 */
public class LDAPIdentityStoreTestCase extends AbstractLDAPTest {
    @Before
    public void setup() throws Exception {
        super.setup();
        importLDIF("ldap/users.ldif");
    }

    private LDAPConfiguration getConfiguration() {
        String fqn = LDAPConfigurationBuilder.class.getName();
        LDAPConfiguration config = (LDAPConfiguration) IdentityStoreConfigurationBuilder.config(fqn);

        config.setBindDN(adminDN).setBindCredential(adminPW).setLdapURL("ldap://localhost:10389");
        config.setUserDNSuffix("ou=People,dc=jboss,dc=org").setRoleDNSuffix("ou=Roles,dc=jboss,dc=org");
        config.setGroupDNSuffix("ou=Groups,dc=jboss,dc=org");
        return config;
    }

    @Test @Ignore
    public void testLDAPIdentityStore() throws Exception {
        LDAPIdentityStore store = new LDAPIdentityStore();

        store.setConfiguration(getConfiguration());

        // Users
        LDAPUser user = new LDAPUser();
        user.setId("Anil Saldhana");
        
        store.createUser(null, user);
        assertNotNull(user);

        User anil = store.getUser(null, "Anil Saldhana");
        assertNotNull(anil);
        assertEquals("Anil Saldhana", anil.getFullName());
        assertEquals("Anil", anil.getFirstName());
        assertEquals("Saldhana", anil.getLastName());

        // Roles
        Role role = store.createRole(null, "testRole");
        assertNotNull(role);
        assertEquals("testRole", role.getName());

        Role ldapRole = store.getRole(null, "testRole");
        assertNotNull(ldapRole);
        assertEquals("testRole", ldapRole.getName());

        // Groups
        Group ldapGroup = store.createGroup(null, "PicketBox Team", null);
        assertNotNull(ldapGroup);

        Group retrievedLDAPGroup = store.getGroup(null, "PicketBox Team");
        assertNotNull(retrievedLDAPGroup);
        assertNull(retrievedLDAPGroup.getParentGroup());

        // Parent Groups Now
        Group devGroup = store.createGroup(null, "Dev", ldapGroup);
        assertNotNull(devGroup);

        Group retrievedDevGroup = store.getGroup(null, "Dev");
        assertNotNull(retrievedDevGroup);
        Group parentOfDevGroup = retrievedDevGroup.getParentGroup();
        assertNotNull(parentOfDevGroup);
        assertEquals("PicketBox Team", parentOfDevGroup.getName());

        // Add a relationship between an user, role and group
        Membership membership = store.createMembership(null, ldapRole, anil, ldapGroup);
        assertNotNull(membership);

        // Deal with removal of users, roles and groups
        store.removeMembership(null, ldapRole, anil, ldapGroup);

        store.removeUser(null, anil);
        store.removeRole(null, ldapRole);
        store.removeGroup(null, ldapGroup);
        store.removeGroup(null, devGroup);

        anil = store.getUser(null, "Anil Saldhana");
        assertNull(anil);
        ldapRole = store.getRole(null, "testRole");
        assertNull(ldapRole);
        assertNull(store.getGroup(null, "Dev"));
        assertNull(store.getGroup(null, "PicketBox Team"));
    }
}