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

import java.util.Map;

import org.junit.Test;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.LDAPIdentityStore;
import org.picketlink.idm.internal.config.LDAPConfiguration;
import org.picketlink.idm.internal.config.LDAPConfigurationBuilder;
import org.picketlink.idm.internal.ldap.LDAPUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.spi.IdentityStoreConfigurationBuilder;

/**
 * Unit test the ability to add custom attributes to {@link LDAPUser}
 *
 * @author anil saldhana
 * @since Sep 7, 2012
 */
public class LDAPCustomAttributesTestCase extends AbstractLDAPIdentityManagerTestCase {

    public void setup() throws Exception {
        super.setup();
        importLDIF("ldap/users.ldif");
    }

    @Test
    public void testUserAttributes() throws Exception {
        LDAPIdentityStore store = new LDAPIdentityStore();
        store.setConfiguration(getConfiguration());

        DefaultIdentityManager im = new DefaultIdentityManager();
        im.setIdentityStore(store); // TODO: wiring needs a second look

        // Let us create an user
        User user = im.createUser("Anil Saldhana");
        assertNotNull(user);

        User anil = im.getUser("Anil Saldhana");
        assertNotNull(anil);
        assertEquals("Anil Saldhana", anil.getFullName());
        assertEquals("Anil", anil.getFirstName());
        assertEquals("Saldhana", anil.getLastName());

        // Deal with Anil's attributes
        store.setAttribute(anil, "QuestionTotal", new String[] { "2" });
        store.setAttribute(anil, "Question1", new String[] { "What is favorite toy?" });
        store.setAttribute(anil, "Question1Answer", new String[] { "Gum" });

        store.setAttribute(anil, "Question2", new String[] { "What is favorite word?" });
        store.setAttribute(anil, "Question2Answer", new String[] { "Hi" });

        // let us retrieve the attributes from ldap store and see if they are the same
        anil = im.getUser("Anil Saldhana");
        Map<String, String[]> attributes = anil.getAttributes();
        assertNotNull(attributes);

        assertEquals("2", attributes.get("QuestionTotal")[0]);
        assertEquals("What is favorite toy?", attributes.get("Question1")[0]);
        assertEquals("Gum", attributes.get("Question1Answer")[0]);
        assertEquals("What is favorite word?", attributes.get("Question2")[0]);
        assertEquals("Hi", attributes.get("Question2Answer")[0]);
    }

    private LDAPConfiguration getConfiguration() {
        String fqn = LDAPConfigurationBuilder.class.getName();
        LDAPConfiguration config = (LDAPConfiguration) IdentityStoreConfigurationBuilder.config(fqn);

        config.setBindDN(adminDN).setBindCredential(adminPW).setLdapURL("ldap://localhost:10389");
        config.setUserDNSuffix("ou=People,dc=jboss,dc=org").setRoleDNSuffix("ou=Roles,dc=jboss,dc=org");
        config.setGroupDNSuffix("ou=Groups,dc=jboss,dc=org");
        return config;
    }

}