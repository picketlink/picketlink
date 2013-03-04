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

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;
import org.picketlink.idm.ldap.internal.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
import org.picketlink.test.idm.relationship.CustomRelationship;

/**
 * <p>
 * Test case for the {@link JPAIdentityStoreConfiguration}.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
public class LDAPIdentityStoreConfigurationTestCase extends
        AbstractFeaturesSetConfigurationTestCase<LDAPIdentityStoreConfiguration> {

    private static final String BASE_DN = "dc=jboss,dc=org";
    private static final String LDAP_URL = "ldap://localhost:10389";
    private static final String ROLES_DN_SUFFIX = "ou=Roles,dc=jboss,dc=org";
    private static final String GROUP_DN_SUFFIX = "ou=Groups,dc=jboss,dc=org";
    private static final String USER_DN_SUFFIX = "ou=People,dc=jboss,dc=org";
    private static final String AGENT_DN_SUFFIX = "ou=Agent,dc=jboss,dc=org";

    private static AbstractLDAPTest instance;

    @BeforeClass
    public static void onBeforeClass() {
        try {
            instance = new AbstractLDAPTest() {

                @Override
                @Before
                public void setup() throws Exception {
                    super.setup();
                    importLDIF("ldap/users.ldif");
                }
            };
            instance.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void onDestroyClass() {
        try {
            instance.tearDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @Test
    public void failFeatureNotSupportedCustomRelationship() {
        IdentityConfiguration config = new IdentityConfiguration();

        LDAPIdentityStoreConfiguration storeConfig = createMinimalConfiguration();

        storeConfig.getFeatureSet().setSupportsCustomRelationships(true);

        config.addStoreConfiguration(storeConfig);

        IdentityManager identityManager = createIdentityManager(config);

        User user = new SimpleUser("someUser");

        identityManager.add(user);

        Role role = new SimpleRole("someRole");

        identityManager.add(role);

        CustomRelationship customRelationship = new CustomRelationship();

        customRelationship.setIdentityTypeA(user);
        customRelationship.setIdentityTypeB(role);

        try {
            identityManager.add(customRelationship);

            fail();
        } catch (SecurityConfigurationException sce) {
            Assert.assertTrue(sce.getMessage().contains("[" + CustomRelationship.class.getName() + "]"));
        } catch (Exception e) {
            Assert.fail();
        }
    }

    @Override
    protected LDAPIdentityStoreConfiguration createMinimalConfiguration() {
        LDAPIdentityStoreConfiguration fileConfig = new LDAPIdentityStoreConfiguration();

        fileConfig.setBaseDN(BASE_DN).setBindDN("uid=admin,ou=system").setBindCredential("secret").setLdapURL(LDAP_URL)
                .setUserDNSuffix(USER_DN_SUFFIX).setRoleDNSuffix(ROLES_DN_SUFFIX).setAgentDNSuffix(AGENT_DN_SUFFIX)
                .setGroupDNSuffix(GROUP_DN_SUFFIX);

        fileConfig.addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org");

        FeatureSet.addFeatureSupport(fileConfig.getFeatureSet());
        FeatureSet.addRelationshipSupport(fileConfig.getFeatureSet());

        // enabled basic features for identitytypes
        FeatureSet.addFeatureSupport(fileConfig.getFeatureSet(), FeatureGroup.user);
        FeatureSet.addFeatureSupport(fileConfig.getFeatureSet(), FeatureGroup.role);
        FeatureSet.addFeatureSupport(fileConfig.getFeatureSet(), FeatureGroup.group);

        // enable relationship features. this enables the default/built-in relationship classes
        FeatureSet.addFeatureSupport(fileConfig.getFeatureSet(), FeatureGroup.relationship);

        // The LDAP store should ignore this feature. LDAP does not supports custom
        // relationships
        FeatureSet.addRelationshipSupport(fileConfig.getFeatureSet(), CustomRelationship.class);

        // to enable custom relationship classes we need to set this flag. This flag should be ignored by the LDAP store.
        fileConfig.getFeatureSet().setSupportsCustomRelationships(true);

        // enable credentials
        FeatureSet.addFeatureSupport(fileConfig.getFeatureSet(), FeatureGroup.credential);

        return fileConfig;
    }

    @Override
    protected IdentityManager createIdentityManager(IdentityConfiguration config) {
        IdentityManager identityManager = new DefaultIdentityManager();

        DefaultIdentityStoreInvocationContextFactory icf = new DefaultIdentityStoreInvocationContextFactory();

        identityManager.bootstrap(config, icf);

        return identityManager;
    }

}
