package org.picketlink.test.idm.partition;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.DefaultPartitionManager;
import org.picketlink.idm.jpa.model.sample.simple.AccountTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.AttributeTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.DigestCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.GroupTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.IdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.OTPCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PartitionTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.PasswordCredentialTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipIdentityTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RelationshipTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.RoleTypeEntity;
import org.picketlink.idm.jpa.model.sample.simple.X509CredentialTypeEntity;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.basic.MyCustomAccountEntity;
import org.picketlink.test.idm.relationship.CustomRelationshipTypeEntity;
import org.picketlink.test.idm.util.JPAContextInitializer;
import org.picketlink.test.idm.util.LDAPEmbeddedServer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.io.Serializable;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.picketlink.idm.ldap.internal.LDAPConstants.*;

/**
 * @author pedroigor
 */
public class MultiplePartitionTestCase {

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    private final LDAPEmbeddedServer embeddedServer = new LDAPEmbeddedServer();

    @Test
    public void testLDAPPartition() {
        PartitionManager partitionManager = getPartitionManager();

        Realm ldapManagedPartition = new Realm("ldap.managed.partition");

        partitionManager.add(ldapManagedPartition, "ldap.config");

        Realm jpaManagedPartition = new Realm("jpa.managed.partition");

        partitionManager.add(jpaManagedPartition, "jpa.config");

        IdentityManager ldapIdentityManager = partitionManager.createIdentityManager(ldapManagedPartition);

        User ldapJohn = new User("john");

        ldapIdentityManager.add(ldapJohn);

        User storeLdapJohn = BasicModel.getUser(ldapIdentityManager, ldapJohn.getLoginName());

        assertNotNull(storeLdapJohn);
        assertEquals(ldapJohn.getId(), storeLdapJohn.getId());

        User jpaJohn = new User("john");

        IdentityManager jpaIdentityManager = partitionManager.createIdentityManager(jpaManagedPartition);

        jpaIdentityManager.add(jpaJohn);

        User storeJpaJohn = BasicModel.getUser(jpaIdentityManager, jpaJohn.getLoginName());

        assertNotNull(storeJpaJohn);
        assertEquals(jpaJohn.getId(), storeJpaJohn.getId());

        assertFalse(storeJpaJohn.getId().equals(storeLdapJohn.getId()));

        storeLdapJohn.setAttribute(new Attribute<Serializable>("name", "value"));

        ldapIdentityManager.update(storeLdapJohn);

        storeLdapJohn = BasicModel.getUser(ldapIdentityManager, ldapJohn.getLoginName());

        // ldap store does not support ad-hoc attributes
        assertNull(storeLdapJohn.getAttribute("name"));

        storeJpaJohn.setAttribute(new Attribute<Serializable>("name", "value"));

        jpaIdentityManager.update(storeJpaJohn);

        storeLdapJohn = BasicModel.getUser(jpaIdentityManager, ldapJohn.getLoginName());

        // jpa store supports ad-hoc attributes
        assertNotNull(storeJpaJohn.getAttribute("name"));
    }

    @Before
    public void onBefore() {
        try {
            this.embeddedServer.setup();
            this.embeddedServer.importLDIF("ldap/users.ldif");
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }

        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @After
    public void onAfter() {
        try {
            this.embeddedServer.tearDown();
        } catch (Exception e) {
            throw new RuntimeException("Error starting Embedded LDAP server.", e);
        }
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }

    public PartitionManager getPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .named("ldap.config")
                .stores()
                    .ldap()
                        .baseDN(embeddedServer.getBaseDn())
                        .bindDN(embeddedServer.getBindDn())
                        .bindCredential(embeddedServer.getBindCredential())
                        .url(embeddedServer.getConnectionUrl())
                        .supportAllFeatures()
                        .supportGlobalRelationship(Grant.class, GroupMembership.class)
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
                        .mapping(Role.class)
                            .baseDN(embeddedServer.getRolesDnSuffix())
                            .objectClasses(GROUP_OF_NAMES)
                            .attribute("name", CN, true)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                        .mapping(Group.class)
                            .baseDN(embeddedServer.getGroupDnSuffix())
                            .objectClasses(GROUP_OF_NAMES)
                            .attribute("name", CN, true)
                            .readOnlyAttribute("createdDate", CREATE_TIMESTAMP)
                            .parentMembershipAttributeName("member")
                            .parentMapping("QA Group", "ou=QA," + embeddedServer.getGroupDnSuffix())
                        .mapping(Grant.class)
                            .forMapping(Role.class)
                            .attribute("assignee", "member")
                        .mapping(GroupMembership.class)
                            .forMapping(Group.class)
                            .attribute("member", "member")
            .named("jpa.config")
                .stores()
                    .jpa()
                        .mappedEntity(
                                PartitionTypeEntity.class,
                                MyCustomAccountEntity.class,
                                RoleTypeEntity.class,
                                GroupTypeEntity.class,
                                IdentityTypeEntity.class,
                                CustomRelationshipTypeEntity.class,
                                CustomPartitionEntity.class,
                                RelationshipTypeEntity.class,
                                RelationshipIdentityTypeEntity.class,
                                PasswordCredentialTypeEntity.class,
                                DigestCredentialTypeEntity.class,
                                X509CredentialTypeEntity.class,
                                OTPCredentialTypeEntity.class,
                                AttributeTypeEntity.class,
                                AccountTypeEntity.class
                        )
                        .supportGlobalRelationship(org.picketlink.idm.model.Relationship.class)
                        .addContextInitializer(new JPAContextInitializer(null) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager;
                            }
                        })
                        .supportAllFeatures();

        return new DefaultPartitionManager(builder.buildAll());
    }
}
