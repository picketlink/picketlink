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
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.model.sample.simple.AttributedTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.test.idm.basic.AttributeReferenceTypeEntity;
import org.picketlink.test.idm.relationship.CustomRelationshipTypeEntity;
import org.picketlink.test.idm.relationship.RelationshipIdentityTypeReferenceEntity;
import org.picketlink.test.idm.util.LDAPEmbeddedServer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.picketlink.idm.ldap.internal.LDAPConstants.*;
import static org.picketlink.test.idm.util.LDAPEmbeddedServer.*;

/**
 * @author pedroigor
 */
public class MixedLDAPJPAStoreConfigurationTester implements IdentityConfigurationTester {

    private final LDAPEmbeddedServer embeddedServer = new LDAPEmbeddedServer();

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    public static final String SIMPLE_JPA_STORE_CONFIG = "SIMPLE_JPA_STORE_CONFIG";
    public static final String SIMPLE_LDAP_STORE_CONFIG = "SIMPLE_LDAP_STORE_CONFIG";

    @Override
    public DefaultPartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named(SIMPLE_JPA_STORE_CONFIG)
                .stores()
                    .jpa()
                        .mappedEntity(
                                AttributedTypeEntity.class,
                                RelationshipTypeEntity.class,
                                RelationshipIdentityTypeReferenceEntity.class,
                                CustomRelationshipTypeEntity.class,
                                AttributeReferenceTypeEntity.class
                        )
                        .addContextInitializer(new JPAContextInitializer(null) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager;
                            }
                        })
                        .supportGlobalRelationship(Relationship.class)
                        .supportAttributes(true)
            .named(SIMPLE_LDAP_STORE_CONFIG)
                .stores()
                    .ldap()
                        .baseDN(BASE_DN)
                        .bindDN("uid=admin,ou=system")
                        .bindCredential("secret")
                        .url(LDAP_URL)
                        .supportType(IdentityType.class)
                        .mapping(Agent.class)
                            .baseDN(AGENT_DN_SUFFIX)
                            .objectClasses("account")
                            .attribute("loginName", UID, true)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                        .mapping(User.class)
                            .baseDN(LDAPEmbeddedServer.USER_DN_SUFFIX)
                            .objectClasses("inetOrgPerson", "organizationalPerson")
                            .attribute("loginName", UID, true)
                            .attribute("firstName", CN)
                            .attribute("lastName", SN)
                            .attribute("email", EMAIL)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                        .mapping(Role.class)
                            .baseDN(LDAPEmbeddedServer.ROLES_DN_SUFFIX)
                            .objectClasses(GROUP_OF_NAMES)
                            .attribute("name", CN, true)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                        .mapping(Group.class)
                            .baseDN(LDAPEmbeddedServer.GROUP_DN_SUFFIX)
                            .objectClasses(GROUP_OF_NAMES)
                            .attribute("name", CN, true)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                            .parentMembershipAttributeName("member")
                            .parentMapping("QA Group", "ou=QA,dc=jboss,dc=org");

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

        this.emf = Persistence.createEntityManagerFactory("ldap-jpa-store-test-pu");
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
