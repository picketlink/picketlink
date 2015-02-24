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

package org.picketlink.test.idm.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.test.idm.util.LDAPEmbeddedServer;

import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.picketlink.common.constants.LDAPConstants.CN;
import static org.picketlink.common.constants.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.EMAIL;
import static org.picketlink.common.constants.LDAPConstants.MODIFY_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.SN;
import static org.picketlink.common.constants.LDAPConstants.UID;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPWithoutObjectClassMappingTestCase {

    public static final String SIMPLE_LDAP_STORE_CONFIG = "SIMPLE_LDAP_STORE_CONFIG";
    private final LDAPEmbeddedServer embeddedServer = new LDAPEmbeddedServer();
    private PartitionManager partitionManager;

    @Before
    public void beforeTest() {
        try {
            this.embeddedServer.setup();
            this.embeddedServer.importLDIF("ldap/default-identities.ldif");
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
    public void testQueryWithNoFilters() throws Exception {
        IdentityManager identityManager = this.partitionManager.createIdentityManager();

        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();

        List result = queryBuilder.createIdentityQuery(User.class).getResultList();

        assertEquals(1, result.size());

        User user = (User) result.get(0);

        assertEquals("guest", user.getLoginName());

        result = queryBuilder.createIdentityQuery(Role.class).getResultList();

        assertEquals(1, result.size());

        Role role = (Role) result.get(0);

        assertEquals("RoleA", role.getName());

        RelationshipManager relationshipManager = this.partitionManager.createRelationshipManager();

        relationshipManager.add(new Grant(user, role));

        RelationshipQuery<Grant> relationshipQuery = relationshipManager.createRelationshipQuery(Grant.class);

        relationshipQuery
                .setParameter(Grant.ASSIGNEE, user)
                .setParameter(Grant.ROLE, role);

        result = relationshipQuery.getResultList();

        assertEquals(1, result.size());

        Grant grant = (Grant) result.get(0);

        assertEquals(user.getId(), grant.getAssignee().getId());
        assertEquals(role.getId(), grant.getRole().getId());
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
                            .attribute("loginName", UID, true)
                            .attribute("firstName", CN)
                            .attribute("lastName", SN)
                            .attribute("email", EMAIL)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                            .readOnlyAttribute("modifyDate", MODIFY_TIMESTAMP)
                        .mapping(Role.class)
                            .baseDN(embeddedServer.getRolesDnSuffix())
                            .attribute("name", CN, true)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                            .readOnlyAttribute("modifyDate", MODIFY_TIMESTAMP)
                        .mapping(Grant.class)
                            .forMapping(Role.class)
                            .attribute("assignee", "member");

        return new DefaultPartitionManager(builder.buildAll());
    }
}
