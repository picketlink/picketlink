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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.SimpleGroup;

/**
 * <p>
 * Test case for {@link Group} basic management operations.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class GroupManagementTestCase extends AbstractLDAPTest {

    private static final String USER_DN_SUFFIX = "ou=People,dc=jboss,dc=org";

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        importLDIF("ldap/users.ldif");
    }

    /**
     * <p>
     * Creates a new {@link Group} instance using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testCreate() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group newGroupInstance = new SimpleGroup("someGroup");

        // let's create the new role
        identityManager.createGroup(newGroupInstance);

        // let's retrieve the role information and see if they are properly stored
        Group storedGroupInstance = identityManager.getGroup(newGroupInstance.getName());

        assertNotNull(storedGroupInstance);
        
        assertEquals(newGroupInstance.getKey(), storedGroupInstance.getKey());
        assertEquals(newGroupInstance.getName(), storedGroupInstance.getName());
    }
    
    /**
     * <p>
     * Creates a new {@link Group} instance as a child of another {@link Group} using the API. This method also checks if the user was properly created by retrieving
     * his information from the store.
     * </p>
     * 
     * @throws Exception
     */
    @Test
    public void testCreateWithParentGroup() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group parentGroup = new SimpleGroup("parentGroup");
        
        identityManager.createGroup(parentGroup);
        
        Group childGroup = new SimpleGroup("childGroup", parentGroup);

        // let's create the new role
        identityManager.createGroup(childGroup);

        // let's retrieve the role information and see if they are properly stored
        Group storedChildGroup = identityManager.getGroup(childGroup.getName());

        assertNotNull(storedChildGroup);
        assertNotNull(storedChildGroup.getParentGroup());
        
        assertEquals(childGroup.getKey(), storedChildGroup.getKey());
        assertEquals(childGroup.getName(), storedChildGroup.getName());
        assertEquals(childGroup.getParentGroup().getName(), storedChildGroup.getParentGroup().getName());
    }

    /**
     * <p>Loads from the LDAP tree an already stored role.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");

        assertNotNull(storedGroupInstance);

        assertEquals("GROUP:///Test Parent Group/Test Group", storedGroupInstance.getKey());
        assertEquals("Test Group", storedGroupInstance.getName());
    }
    
    /**
     * <p>Remove from the LDAP tree an already stored role.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testRemove() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");

        assertNotNull(storedGroupInstance);
        
        identityManager.removeGroup(storedGroupInstance);
        
        Group removedGroupInstance = identityManager.getGroup("Test Group");
        
        assertNull(removedGroupInstance);
    }
    
    /**
     * <p>Sets an one-valued attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testSetOneValuedAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");
        
        storedGroupInstance.setAttribute(new Attribute<String>("one-valued", "1"));
        
//        identityManager.updateGroup(storedGroupInstance);
        
//        Group updatedGroupInstance = identityManager.getGroup(storedGroupInstance.getId());
        
//        Attribute<String> oneValuedAttribute = updatedGroupInstance.getAttribute("one-valued");
        
//        assertNotNull(oneValuedAttribute);
//        assertEquals("1", oneValuedAttribute.getValue());
    }
    
    /**
     * <p>Sets a multi-valued attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testSetMultiValuedAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");
        
        storedGroupInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] {"1", "2", "3"}));
        
//        identityManager.updateGroup(storedGroupInstance);
//        
//        Group updatedGroupInstance = identityManager.getGroup(storedGroupInstance.getId());
//        
//        Attribute<String[]> multiValuedAttribute = updatedGroupInstance.getAttribute("multi-valued");
//        
//        assertNotNull(multiValuedAttribute);
//        assertEquals("1", multiValuedAttribute.getValue()[0]);
//        assertEquals("2", multiValuedAttribute.getValue()[1]);
//        assertEquals("3", multiValuedAttribute.getValue()[2]);
    }
    
    /**
     * <p>Updates an attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");
        
        storedGroupInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] {"1", "2", "3"}));
        
//        identityManager.updateGroup(storedGroupInstance);
//        
//        Group updatedGroupInstance = identityManager.getGroup(storedGroupInstance.getId());
//        
//        Attribute<String[]> multiValuedAttribute = updatedGroupInstance.getAttribute("multi-valued");
//        
//        assertNotNull(multiValuedAttribute);
//
//        multiValuedAttribute.setValue(new String[] {"3", "4", "5"});
//        
//        updatedGroupInstance.setAttribute(multiValuedAttribute);
//        
//        identityManager.updateGroup(updatedGroupInstance);
//        
//        updatedGroupInstance = identityManager.getGroup("Administrator");
//        
//        multiValuedAttribute = updatedGroupInstance.getAttribute("multi-valued");
//        
//        assertNotNull(multiValuedAttribute);
//        assertEquals("3", multiValuedAttribute.getValue()[0]);
//        assertEquals("4", multiValuedAttribute.getValue()[1]);
//        assertEquals("5", multiValuedAttribute.getValue()[2]);
    }
    
    /**
     * <p>Removes an attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        Group storedGroupInstance = identityManager.getGroup("Test Group");
        
        storedGroupInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] {"1", "2", "3"}));
        
//        identityManager.updateGroup(storedGroupInstance);
//        
//        Group updatedGroupInstance = identityManager.getGroup(storedGroupInstance.getId());
//        
//        Attribute<String[]> multiValuedAttribute = updatedGroupInstance.getAttribute("multi-valued");
//        
//        assertNotNull(multiValuedAttribute);
//        
//        updatedGroupInstance.removeAttribute("multi-valued");
//        
//        identityManager.updateGroup(updatedGroupInstance);
//        
//        updatedGroupInstance = identityManager.getGroup("Administrator");
//        
//        multiValuedAttribute = updatedGroupInstance.getAttribute("multi-valued");
//        
//        assertNull(multiValuedAttribute);
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
