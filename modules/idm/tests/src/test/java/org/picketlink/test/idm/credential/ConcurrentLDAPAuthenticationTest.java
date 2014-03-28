/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.test.idm.credential;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.util.LDAPEmbeddedServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.common.constants.LDAPConstants.CN;
import static org.picketlink.common.constants.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.EMAIL;
import static org.picketlink.common.constants.LDAPConstants.SN;
import static org.picketlink.common.constants.LDAPConstants.UID;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ConcurrentLDAPAuthenticationTest {

    public static final String SIMPLE_LDAP_STORE_CONFIG = "SIMPLE_LDAP_STORE_CONFIG";
    private final LDAPEmbeddedServer embeddedServer = new LDAPEmbeddedServer();
    private PartitionManager partitionManager;

    @Before
    public void beforeTest() {
        try {
            this.embeddedServer.setup();
            this.embeddedServer.importLDIF("ldap/users.ldif");
            this.partitionManager = getPartitionManager();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }
    }

    @After
    public void afterTest() {
        try {
            this.embeddedServer.tearDown();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }
    }

    @Test
    public void testConcurrentPasswordValidation() throws Exception {
        IdentityManager identityManager = partitionManager.createIdentityManager();

        User evil = new User("evil");
        identityManager.add(evil);
        User user = new User("user");
        identityManager.add(user);

        identityManager.updateCredential(evil, new Password("evilpassword".toCharArray()));
        identityManager.updateCredential(user, new Password("password".toCharArray()));

        final AtomicBoolean userValidated = new AtomicBoolean(false);
        Thread t = new Thread() {

            @Override
            public void run() {
                IdentityManager identityManager = partitionManager.createIdentityManager();
                UsernamePasswordCredentials credential = new UsernamePasswordCredentials();
                credential.setUsername("user");
                credential.setPassword(new Password("password".toCharArray()));
                identityManager.validateCredentials(credential);
                assertEquals(Credentials.Status.VALID, credential.getStatus());
                assertNotNull(credential.getValidatedAccount());
                userValidated.set(true);
            }

        };
        t.start();

        UsernamePasswordCredentials credential = new UsernamePasswordCredentials();
        credential.setUsername("evil");
        credential.setPassword(new Password("invalid".toCharArray()));
        identityManager.validateCredentials(credential);
        assertEquals(Credentials.Status.INVALID, credential.getStatus());

        t.join(30000);
        assertTrue(userValidated.get());
    }

    private PartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named(SIMPLE_LDAP_STORE_CONFIG)
                .stores()
                    .ldap()
                        .baseDN(embeddedServer.getBaseDn())
                        .bindDN(embeddedServer.getBindDn())
                        .bindCredential(embeddedServer.getBindCredential())
                        .url(embeddedServer.getConnectionUrl())
                        .supportAllFeatures()
                        .mapping(Agent.class)
                            .baseDN(embeddedServer.getAgentDnSuffix())
                .objectClasses("account")
                .attribute("loginName", UID, true)
                .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                        .mapping(User.class)
                            .baseDN(embeddedServer.getUserDnSuffix())
                            .objectClasses("inetOrgPerson", "organizationalPerson")
                            .attribute("loginName", UID, true)
                            .attribute("firstName", CN)
                            .attribute("lastName", SN)
                            .attribute("email", EMAIL)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP);

        return new DefaultPartitionManager(builder.buildAll());
    }
}
