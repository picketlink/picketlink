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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import org.picketlink.idm.model.User;

/**
 * <p>
 * Test case for {@link User} attribute management.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class UserAttributeManagementTestCase extends AbstractLDAPTest {

    private static final String USER_DN_SUFFIX = "ou=People,dc=jboss,dc=org";

    @Before
    @Override
    public void setup() throws Exception {
        super.setup();
        importLDIF("ldap/users.ldif");
    }

    /**
     * <p>Sets an one-valued attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testSetOneValuedAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");
        
        storedUserInstance.setAttribute(new Attribute<String>("one-valued", "1"));
        
        identityManager.updateUser(storedUserInstance);
        
        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());
        
        Attribute<String> oneValuedAttribute = updatedUserInstance.getAttribute("one-valued");
        
        assertNotNull(oneValuedAttribute);
        assertEquals("1", oneValuedAttribute.getValue());
    }
    
    /**
     * <p>Sets a multi-valued attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testSetMultiValuedAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");
        
        storedUserInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] {"1", "2", "3"}));
        
        identityManager.updateUser(storedUserInstance);
        
        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());
        
        Attribute<String[]> multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");
        
        assertNotNull(multiValuedAttribute);
        assertEquals("1", multiValuedAttribute.getValue()[0]);
        assertEquals("2", multiValuedAttribute.getValue()[1]);
        assertEquals("3", multiValuedAttribute.getValue()[2]);
    }
    
    /**
     * <p>Updates an attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testUpdateAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");
        
        storedUserInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] {"1", "2", "3"}));
        
        identityManager.updateUser(storedUserInstance);
        
        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());
        
        Attribute<String[]> multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");
        
        assertNotNull(multiValuedAttribute);

        multiValuedAttribute.setValue(new String[] {"3", "4", "5"});
        
        updatedUserInstance.setAttribute(multiValuedAttribute);
        
        identityManager.updateUser(updatedUserInstance);
        
        updatedUserInstance = identityManager.getUser("admin");
        
        multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");
        
        assertNotNull(multiValuedAttribute);
        assertEquals("3", multiValuedAttribute.getValue()[0]);
        assertEquals("4", multiValuedAttribute.getValue()[1]);
        assertEquals("5", multiValuedAttribute.getValue()[2]);

    }
    
    /**
     * <p>Removes an attribute.</p>
     * 
     * @throws Exception
     */
    @Test
    public void testRemoveAttribute() throws Exception {
        IdentityManager identityManager = getIdentityManager();

        User storedUserInstance = identityManager.getUser("admin");
        
        storedUserInstance.setAttribute(new Attribute<String[]>("multi-valued", new String[] {"1", "2", "3"}));
        
        identityManager.updateUser(storedUserInstance);
        
        User updatedUserInstance = identityManager.getUser(storedUserInstance.getId());
        
        Attribute<String[]> multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");
        
        assertNotNull(multiValuedAttribute);
        
        updatedUserInstance.removeAttribute("multi-valued");
        
        identityManager.updateUser(updatedUserInstance);
        
        updatedUserInstance = identityManager.getUser("admin");
        
        multiValuedAttribute = updatedUserInstance.getAttribute("multi-valued");
        
        assertNull(multiValuedAttribute);
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
