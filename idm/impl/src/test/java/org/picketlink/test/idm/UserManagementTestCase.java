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

package org.picketlink.test.idm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPUser;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

/**
 * <p>
 * Test case for {@link User} basic management operations.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class UserManagementTestCase extends AbstractLDAPTest {

    private static final String USER_DN_SUFFIX = "ou=People,dc=jboss,dc=org";

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        importLDIF("ldap/users.ldif");
    }

    /**
     * <p>
     * Creates a new {@link User} instance using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     * 
     * @throws Exception
     */
    @Test @Ignore
    public void testCreate() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User newUserInstance = new SimpleUser("jduke");

        newUserInstance.setEmail("jduke@jboss.org");
        newUserInstance.setFirstName("Java");
        newUserInstance.setLastName("Duke");
        
        // let's create the new user
        identityManager.createUser(newUserInstance);

        // let's retrieve the user information and see if they are properly stored
        User storedUserInstance = identityManager.getUser(newUserInstance.getId());

        assertNotNull(storedUserInstance);
        
        // the user instance returned by the store must be a LDAPUser instance
        assertTrue(storedUserInstance instanceof LDAPUser);

        assertEquals(newUserInstance.getId(), storedUserInstance.getId());
        assertEquals(newUserInstance.getFirstName(), storedUserInstance.getFirstName());
        assertEquals(newUserInstance.getLastName(), storedUserInstance.getLastName());
        assertEquals(newUserInstance.getEmail(), storedUserInstance.getEmail());
        assertEquals(newUserInstance.getFullName(), storedUserInstance.getFullName());
    }

    /**
     * <p>Loads from the LDAP tree an already stored user.</p>
     * 
     * @throws Exception
     */
    @Test @Ignore
    public void testGet() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        assertNotNull(storedUserInstance);
        assertTrue(storedUserInstance instanceof LDAPUser);

        assertEquals("admin", storedUserInstance.getId());
        assertEquals("The", storedUserInstance.getFirstName());
        assertEquals("Administrator", storedUserInstance.getLastName());
        assertEquals("admin@jboss.org", storedUserInstance.getEmail());
        assertEquals("The Administrator", storedUserInstance.getFullName());
    }
    
    /**
     * <p>Updates the stored user information.</p>
     * 
     * @throws Exception
     */
    @Test @Ignore
    public void testUpdate() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        assertNotNull(storedUserInstance);
        assertTrue(storedUserInstance instanceof LDAPUser);

        assertEquals("admin", storedUserInstance.getId());
        assertEquals("The", storedUserInstance.getFirstName());
        assertEquals("Administrator", storedUserInstance.getLastName());
        assertEquals("admin@jboss.org", storedUserInstance.getEmail());
        assertEquals("The Administrator", storedUserInstance.getFullName());
        
        storedUserInstance.setFirstName("Updated " + storedUserInstance.getFirstName());
        storedUserInstance.setLastName("Updated " + storedUserInstance.getLastName());
        storedUserInstance.setEmail("Updated " + storedUserInstance.getEmail());
        
        identityManager.updateUser(storedUserInstance);
        
        User updatedUser = identityManager.getUser(storedUserInstance.getId());
        
        assertEquals("Updated The", updatedUser.getFirstName());
        assertEquals("Updated Administrator", updatedUser.getLastName());
        assertEquals("Updated admin@jboss.org", updatedUser.getEmail());
        assertEquals("Updated The Updated Administrator", updatedUser.getFullName());

    }
    
    /**
     * <p>Remove from the LDAP tree an already stored user.</p>
     * 
     * @throws Exception
     */
    @Test @Ignore
    public void testRemove() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");

        assertNotNull(storedUserInstance);
        
        identityManager.removeUser(storedUserInstance);
        
        User removedUserInstance = identityManager.getUser("admin");
        
        assertNull(removedUserInstance);
    }

    private IdentityManager getIdentityManager() {
        IdentityConfiguration config = new IdentityConfiguration();

        config.addStoreConfiguration(getConfiguration());

        IdentityManager identityManager = new DefaultIdentityManager();

        identityManager.bootstrap(config, new DefaultIdentityStoreInvocationContextFactory(null));
        
        return identityManager;
    }

    private LDAPConfiguration getConfiguration() {
        LDAPConfigurationBuilder builder = new LDAPConfigurationBuilder();
        LDAPConfiguration config = (LDAPConfiguration) builder.build();

        config.setBindDN(adminDN).setBindCredential(adminPW).setLdapURL("ldap://localhost:10389");
        config.setUserDNSuffix(USER_DN_SUFFIX).setRoleDNSuffix("ou=Roles,dc=jboss,dc=org");
        config.setGroupDNSuffix("ou=Groups,dc=jboss,dc=org");

        return config;
    }
}
