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

import org.junit.Before;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.LDAPIdentityStore;
import org.picketlink.idm.internal.config.LDAPConfiguration;
import org.picketlink.idm.internal.config.LDAPConfigurationBuilder;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreConfigurationBuilder;

/**
 * <p>Base class for {@link IdentityManager} test cases using the {@link LDAPIdentityStore}.</p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractLDAPIdentityManagerTestCase extends AbstractLDAPTest {
    
    private IdentityManager identityManager;
    
    @Before
    public void setup() throws Exception {
        super.setup();
        importLDIF("ldap/users.ldif");
    }
    
    protected IdentityManager getIdentityManager() {
        if (this.identityManager == null) {
            DefaultIdentityManager defaultIdentityManager = new DefaultIdentityManager();

            defaultIdentityManager.setIdentityStore(createIdentityStore());

            this.identityManager = defaultIdentityManager;
        }

        return this.identityManager;
    }

    private IdentityStore createIdentityStore() {
        LDAPIdentityStore store = new LDAPIdentityStore();

        store.setConfiguration(getConfiguration());

        return store;
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
