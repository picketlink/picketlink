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
package org.picketlink.test.idm.testers;

import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.basic.AttributeReferenceTypeEntity;
import org.picketlink.test.idm.partition.CustomPartitionEntity;
import org.picketlink.test.idm.relationship.CustomRelationshipTestCase;
import org.picketlink.test.idm.relationship.CustomRelationshipTypeEntity;
import org.picketlink.test.idm.relationship.RelationshipIdentityTypeReferenceEntity;
import org.picketlink.test.idm.util.JPAContextInitializer;
import org.picketlink.test.idm.util.LDAPEmbeddedServer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.picketlink.common.constants.LDAPConstants.CN;
import static org.picketlink.common.constants.LDAPConstants.CREATE_TIMESTAMP;
import static org.picketlink.common.constants.LDAPConstants.EMAIL;
import static org.picketlink.common.constants.LDAPConstants.GROUP_OF_NAMES;
import static org.picketlink.common.constants.LDAPConstants.SN;
import static org.picketlink.common.constants.LDAPConstants.UID;
import static org.picketlink.test.idm.util.PersistenceUtil.createEntityManagerFactory;

/**
 * @author pedroigor
 */
public class LDAPUserGroupJPARoleConfigurationTester implements IdentityConfigurationTester {

    private final LDAPEmbeddedServer embeddedServer = new LDAPEmbeddedServer();

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    public static final String CONFIGURATION_NAME = "LDAPUserGroupJPARoleConfigurationTester";

    @Override
    public DefaultPartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named(CONFIGURATION_NAME)
                .stores()
                    .jpa()
                        .mappedEntity(
                                CustomRelationshipTypeEntity.class,
                                CustomPartitionEntity.class,
                                AttributeReferenceTypeEntity.class,
                                RelationshipTypeEntity.class,
                                RelationshipIdentityTypeReferenceEntity.class,
                                RoleTypeEntity.class,
                                AttributeReferenceTypeEntity.class,
                                PartitionTypeEntity.class,
                                IdentityTypeEntity.class
                        )
                        .addContextInitializer(new JPAContextInitializer(null) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager;
                            }
                        })
                        .supportGlobalRelationship(Grant.class, CustomRelationshipTestCase.CustomRelationship.class)
                        .supportType(Role.class, Partition.class)
                        .supportAttributes(true)
                    .ldap()
                        .baseDN(embeddedServer.getBaseDn())
                        .bindDN(embeddedServer.getBindDn())
                        .bindCredential(embeddedServer.getBindCredential())
                        .url(embeddedServer.getConnectionUrl())
                        .supportCredentials(true)
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
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                        .mapping(Group.class)
                            .baseDN(embeddedServer.getGroupDnSuffix())
                            .hierarchySearchDepth(5)
                            .objectClasses(GROUP_OF_NAMES)
                            .attribute("name", CN, true)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                            .parentMembershipAttributeName("member")
                            .parentMapping("QA Group", "ou=QA," + embeddedServer.getGroupDnSuffix())
                        .mapping(GroupMembership.class)
                            .forMapping(Group.class)
                            .attribute("member", "member");

        DefaultPartitionManager partitionManager = new DefaultPartitionManager(builder.buildAll());

        if (partitionManager.getPartition(Realm.class, Realm.DEFAULT_REALM) == null) {
            partitionManager.add(new Realm(Realm.DEFAULT_REALM));
        }

        return partitionManager;
    }

    @Override
    public void beforeTest() {
        try {
            this.embeddedServer.setup();
            this.embeddedServer.importLDIF("ldap/users.ldif");
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }

        this.emf = createEntityManagerFactory("ldap-usergroup-jpa-role-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @Override
    public void afterTest() {
        try {
            this.embeddedServer.tearDown();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }

        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
