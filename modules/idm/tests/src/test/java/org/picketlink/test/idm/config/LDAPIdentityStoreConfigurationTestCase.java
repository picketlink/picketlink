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
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.LDAPStoreConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.relationship.CustomRelationship;
import org.picketlink.test.idm.suites.LDAPAbstractSuite;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * <p>
 * Test case for the {@link JPAIdentityStoreConfigurationOld}.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
public class LDAPIdentityStoreConfigurationTestCase extends
        AbstractFeaturesSetConfigurationTestCase<LDAPStoreConfigurationBuilder> {

    private static LDAPAbstractSuite instance;

    @BeforeClass
    public static void onBeforeClass() {
        try {
            instance = new LDAPAbstractSuite() {

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

        PartitionManager partitionManager = createPartitionManager(storeConfig.build());
        IdentityManager identityManager = partitionManager.createIdentityManager();

        User user = new User("someUser");

        identityManager.add(user);

        Role role = new Role("someRole");

        identityManager.add(role);

        CustomRelationship customRelationship = new CustomRelationship();

        customRelationship.setIdentityTypeA(user);
        customRelationship.setIdentityTypeB(role);

        try {
            partitionManager.add(customRelationship);

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
                .baseDN(instance.getBaseDn())
                .bindDN(instance.getBindDn())
                .bindCredential(instance.getBindCredential())
                .url(instance.getConnectionUrl())
                .userDNSuffix(instance.getUserDnSuffix())
                .roleDNSuffix(instance.getRolesDnSuffix())
                .agentDNSuffix(instance.getAgentDnSuffix())
                .groupDNSuffix(instance.getGroupDnSuffix())
                .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
                .supportAllFeatures();

        return storeConfig;
    }

}
