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
package org.picketlink.test.idm;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Grant;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.basic.CustomIdentityTypeTestCase;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.EMAIL;
import static org.picketlink.idm.ldap.internal.LDAPConstants.GROUP_OF_NAMES;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.UID;
import static org.picketlink.test.idm.LDAPEmbeddedServer.AGENT_DN_SUFFIX;
import static org.picketlink.test.idm.LDAPEmbeddedServer.BASE_DN;
import static org.picketlink.test.idm.LDAPEmbeddedServer.LDAP_URL;

/**
 * @author pedroigor
 */
public class SimpleLDAPStoreConfigurationTester implements IdentityConfigurationTestVisitor {

    public static final String SIMPLE_LDAP_STORE_CONFIG = "SIMPLE_LDAP_STORE_CONFIG";
    private final LDAPEmbeddedServer embeddedServer = new LDAPEmbeddedServer();

    @Override
    public PartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named(SIMPLE_LDAP_STORE_CONFIG)
                .stores()
                    .ldap()
                        .baseDN(BASE_DN)
                        .bindDN("uid=admin,ou=system")
                        .bindCredential("secret")
                        .url(LDAP_URL)
                        .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
                        .supportType(IdentityType.class)
                        .supportGlobalRelationship(Grant.class, GroupMembership.class)
                        .mapping(Agent.class)
                            .baseDN(AGENT_DN_SUFFIX)
                            .objectClasses("account")
                            .attribute("loginName", UID, true)
                        .mapping(User.class)
                            .baseDN(LDAPEmbeddedServer.USER_DN_SUFFIX)
                            .objectClasses("inetOrgPerson", "organizationalPerson")
                            .attribute("loginName", UID, true)
                            .attribute("firstName", CN)
                            .attribute("lastName", SN)
                            .attribute("email", EMAIL)
                        .mapping(Role.class)
                            .baseDN(LDAPEmbeddedServer.ROLES_DN_SUFFIX)
                            .objectClasses(GROUP_OF_NAMES)
                            .attribute("name", CN, true)
                        .mapping(Group.class)
                            .baseDN(LDAPEmbeddedServer.GROUP_DN_SUFFIX)
                            .objectClasses(GROUP_OF_NAMES)
                            .attribute("name", CN, true)
                            .parentMembershipAttributeName("member")
                        .mapping(CustomIdentityTypeTestCase.MyCustomIdentityType.class)
                            .baseDN("ou=CustomTypes,dc=jboss,dc=org")
                            .objectClasses("device")
                            .attribute("someIdentifier", CN, true)
                            .attribute("someAttribute", "description")
                        .mappingRelationship(Grant.class)
                            .forMapping(Role.class)
                            .attribute("assignee", "member")
                        .mappingRelationship(GroupMembership.class)
                            .forMapping(Group.class)
                            .attribute("member", "member");

        return new DefaultPartitionManager(builder.buildAll());
    }

    @Override
    public void beforeTest() {
        try {
            this.embeddedServer.setup();
            this.embeddedServer.importLDIF("ldap/users.ldif");
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }
    }

    @Override
    public void afterTest() {
        try {
            this.embeddedServer.tearDown();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }
    }
}
