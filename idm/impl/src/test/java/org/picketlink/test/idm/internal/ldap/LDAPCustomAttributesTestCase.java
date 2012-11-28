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

import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoreConfigurationBuilder;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPUser;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreFactory;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.TierStore;

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

    @Test @Ignore
    public void testUserAttributes() throws Exception {
        LDAPConfiguration storeConfig = getConfiguration();
        final LDAPIdentityStore store = new LDAPIdentityStore();

        IdentityStoreInvocationContextFactory isicf = new DefaultIdentityStoreInvocationContextFactory(null);

        store.setup(storeConfig, isicf.createContext());

        IdentityConfiguration config = new IdentityConfiguration();
        config.addStoreConfiguration(storeConfig);

        DefaultIdentityManager im = new DefaultIdentityManager();
        im.setIdentityStoreFactory(new IdentityStoreFactory() {
            @Override
            public IdentityStore createIdentityStore(IdentityStoreConfiguration config, 
                    IdentityStoreInvocationContext ctx) {
                // Just return the store we've already created
                return store;
            }
            @Override
            public void mapConfiguration(Class<? extends IdentityStoreConfiguration> configClass,
                    Class<? extends IdentityStore> storeClass) {
                // noop
            }
            @Override
            public TierStore createTierStore(IdentityStoreConfiguration config) {
                // TODO Auto-generated method stub
                return null;
            }
        });

        im.bootstrap(config, isicf);

        // Let us create an user
        User user = new SimpleUser("Anil Saldhana");
        im.createUser(user);
        assertNotNull(user);

        User anil = im.getUser("Anil Saldhana");
        assertNotNull(anil);
//        assertEquals("Anil Saldhana", anil.getFullName());
        assertEquals("Anil", anil.getFirstName());
        assertEquals("Saldhana", anil.getLastName());

        // Deal with Anil's attributes
        store.setAttribute(anil, new Attribute<String[]>("QuestionTotal", new String[] { "2" }));
        store.setAttribute(anil, new Attribute<String[]>("Question1", new String[] { "What is favorite toy?" }));
        store.setAttribute(anil, new Attribute<String[]>("Question1Answer", new String[] { "Gum" }));

        store.setAttribute(anil, new Attribute<String[]>("Question2", new String[] { "What is favorite word?" }));
        store.setAttribute(anil, new Attribute<String[]>("Question2Answer", new String[] { "Hi" }));

        // let us retrieve the attributes from ldap store and see if they are the same
        anil = im.getUser("Anil Saldhana");
        assertNotNull(anil.getAttributes());

        assertEquals("2", anil.<String[]>getAttribute("QuestionTotal").getValue()[0]);
        assertEquals("What is favorite toy?", anil.<String[]>getAttribute("Question1").getValue()[0]);
        assertEquals("Gum", anil.<String[]>getAttribute("Question1Answer").getValue()[0]);
        assertEquals("What is favorite word?", anil.<String[]>getAttribute("Question2").getValue()[0]);
        assertEquals("Hi", anil.<String[]>getAttribute("Question2Answer").getValue()[0]);
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