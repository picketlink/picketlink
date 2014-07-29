/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.picketlink.test.idm.usecases;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.test.idm.util.LDAPEmbeddedServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.picketlink.common.constants.LDAPConstants.CN;
import static org.picketlink.common.constants.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.EMAIL;
import static org.picketlink.common.constants.LDAPConstants.MODIFY_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.SN;
import static org.picketlink.common.constants.LDAPConstants.UID;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPUseCasesTestCase {

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

    // Ignored by default as pagination doesn't work correctly with ApacheDS
    @Test
    @Ignore
    public void testUsersLDAPPagination() throws Exception {
        IdentityManager identityManager = partitionManager.createIdentityManager();
        int count = 12;

        for (int i=0 ; i<count ; i++) {
            User u = new User("user" + i);
            identityManager.add(u);
        }

        IdentityQuery<User> userQuery = identityManager.createIdentityQuery(User.class);
        List<String> usernames = new ArrayList<String>();

        boolean nextPage = true;
        while (nextPage) {
            userQuery.setLimit(5);
            List<User> users = userQuery.getResultList();
            Assert.assertTrue("Users size is " + users.size(), users.size() <= 5);
            if (users.size() < 5) {
                nextPage = false;
            }

            for (User user : users) {
                usernames.add(user.getLoginName());
            }
        }

        Assert.assertEquals(count, usernames.size());
        for (int i=0 ; i<count ; i++) {
            boolean removed = usernames.remove("user" + i);
            Assert.assertTrue(removed);
        }
    }

    @Test
    public void testDynamicAttributes() throws Exception {
        IdentityManager identityManager = partitionManager.createIdentityManager();
        User john = new User("johny1");
        john.setFirstName("wontBeUsed");
        john.setLastName("Anthon");
        john.setEmail("johny1@email.org");
        john.setAttribute(new Attribute("fooFirstName", "John"));
        identityManager.add(john);

        // modifyDate may be null after creation (depends on LDAP server)
        john = BasicModel.getUser(identityManager, "johny1");
        Assert.assertNotNull(john.getCreatedDate());

        john.setLastName("Anthony");
        identityManager.update(john);

        john = BasicModel.getUser(identityManager, "johny1");
        Assert.assertNull(john.getFirstName());
        Assert.assertEquals("Anthony", john.getLastName());
        Assert.assertEquals("johny1@email.org", john.getEmail());
        Assert.assertEquals("John", john.getAttribute("fooFirstName").getValue());
        Assert.assertNotNull(john.getCreatedDate());
        Assert.assertNotNull(john.getAttribute("modifyDate"));

        identityManager.remove(john);
        Assert.assertNull(BasicModel.getUser(identityManager, "johny1"));
    }

    private PartitionManager getPartitionManager() {
        Properties connectionProps = new Properties();
        connectionProps.put("com.sun.jndi.ldap.connect.pool", "true");

        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named(SIMPLE_LDAP_STORE_CONFIG)
                .stores()
                    .ldap()
                        .connectionProperties(connectionProps)
                        .baseDN(embeddedServer.getBaseDn())
                        .bindDN(embeddedServer.getBindDn())
                        .bindCredential(embeddedServer.getBindCredential())
                        .url(embeddedServer.getConnectionUrl())
                        .supportAllFeatures()
                        .pagination(true)
                        .mapping(User.class)
                            .baseDN(embeddedServer.getUserDnSuffix())
                            .objectClasses("inetOrgPerson", "organizationalPerson")
                            .attribute("loginName", UID, true)
                            .attribute("fooFirstName", CN)
                            .attribute("lastName", SN)
                            .attribute("email", EMAIL)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                            .readOnlyAttribute("modifyDate", MODIFY_TIMESTAMP);

        return new DefaultPartitionManager(builder.buildAll());
    }
}
