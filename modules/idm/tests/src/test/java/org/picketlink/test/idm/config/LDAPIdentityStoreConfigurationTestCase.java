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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.picketbox.test.ldap.AbstractLDAPTest;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.LDAPStoreConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
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
        AbstractFeaturesSetConfigurationTestCase<LDAPStoreConfigurationBuilder> {

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
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        LDAPStoreConfigurationBuilder storeConfig = createMinimalConfiguration(builder);

        IdentityManager identityManager = createIdentityManager(builder.build());

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
        } catch (IdentityManagementException ime) {
        if (SecurityConfigurationException.class.isInstance(ime.getCause())) {
        SecurityConfigurationException sce = (SecurityConfigurationException) ime.getCause();

        assertTrue(sce.getMessage().contains(CustomRelationship.class.getName()));
        } else {
        fail();
        }
        } catch (Exception e) {
        fail();
        }
    }

    @Override
    protected LDAPStoreConfigurationBuilder createMinimalConfiguration(IdentityConfigurationBuilder builder) {
        LDAPStoreConfigurationBuilder storeConfig = builder.stores()
                .ldap()
                .baseDN(BASE_DN)
                .bindDN("uid=admin,ou=system")
                .bindCredential("secret")
                .url(LDAP_URL)
                .userDNSuffix(USER_DN_SUFFIX)
                .roleDNSuffix(ROLES_DN_SUFFIX)
                .agentDNSuffix(AGENT_DN_SUFFIX)
                .groupDNSuffix(GROUP_DN_SUFFIX)
                .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
                .supportFeature(FeatureGroup.user, FeatureGroup.agent, FeatureGroup.user, FeatureGroup.group,
                        FeatureGroup.role, FeatureGroup.attribute, FeatureGroup.relationship, FeatureGroup.credential);

        return storeConfig;
    }

}
