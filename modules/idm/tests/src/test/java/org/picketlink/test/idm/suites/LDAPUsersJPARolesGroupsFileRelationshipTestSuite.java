package org.picketlink.test.idm.suites;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.internal.IdentityManagerFactory;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityWeakObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Authorization;
<<<<<<< HEAD
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
=======
import org.picketlink.idm.model.Realm;
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
import org.picketlink.test.idm.IdentityManagerRunner;
import org.picketlink.test.idm.TestLifecycle;
import org.picketlink.test.idm.basic.AgentManagementTestCase;
import org.picketlink.test.idm.basic.GroupManagementTestCase;
import org.picketlink.test.idm.basic.RoleManagementTestCase;
import org.picketlink.test.idm.basic.UserManagementTestCase;
import org.picketlink.test.idm.credential.PasswordCredentialTestCase;
import org.picketlink.test.idm.query.AgentQueryTestCase;
import org.picketlink.test.idm.query.GroupQueryTestCase;
import org.picketlink.test.idm.query.RelationshipQueryTestCase;
import org.picketlink.test.idm.query.RoleQueryTestCase;
import org.picketlink.test.idm.query.UserQueryTestCase;
import org.picketlink.test.idm.relationship.AgentGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupRoleRelationshipTestCase;
import org.picketlink.test.idm.relationship.AgentGroupsRelationshipTestCase;
import org.picketlink.test.idm.relationship.CustomRelationship;
import org.picketlink.test.idm.relationship.CustomRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.GroupMembershipTestCase;
import org.picketlink.test.idm.relationship.UserGrantRelationshipTestCase;
import org.picketlink.test.idm.relationship.UserGroupRoleRelationshipTestCase;

/**
 * <p>
 * Test suite for the {@link IdentityManager} using a {@link JPAIdentityStore} in conjunction with a {@link LDAPIdentityStore}.
 * This suite tests a common scenario where the LDAP is used to store only agents, users and credentials and the database for
 * roles, groups and relationships.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ RelationshipQueryTestCase.class, CustomRelationshipTestCase.class, PasswordCredentialTestCase.class, UserManagementTestCase.class,
    RoleManagementTestCase.class, GroupManagementTestCase.class, AgentManagementTestCase.class, AgentQueryTestCase.class,
    UserQueryTestCase.class, RoleQueryTestCase.class, GroupQueryTestCase.class, AgentGroupRoleRelationshipTestCase.class,
    AgentGroupsRelationshipTestCase.class, UserGrantRelationshipTestCase.class, AgentGrantRelationshipTestCase.class,
    GroupGrantRelationshipTestCase.class, UserGroupRoleRelationshipTestCase.class, GroupMembershipTestCase.class })
public class LDAPUsersJPARolesGroupsFileRelationshipTestSuite extends LDAPAbstractSuite implements TestLifecycle {

    private static LDAPUsersJPARolesGroupsFileRelationshipTestSuite instance;

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new LDAPUsersJPARolesGroupsFileRelationshipTestSuite();
        }

        return instance;
    }

    @BeforeClass
    public static void onBeforeClass() {
        try {
            init();
            instance.setup();
            instance.importLDIF("ldap/users.ldif");
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
    public void onInit() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @SuppressWarnings("unchecked")
    @Override
    public IdentityManagerFactory createIdentityManagerFactory() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .stores()
                .ldap()
<<<<<<< HEAD
                    .baseDN(BASE_DN)
                    .bindDN("uid=admin,ou=system")
                    .bindCredential("secret")
                    .url(LDAP_URL)
                    .userDNSuffix(USER_DN_SUFFIX)
                    .roleDNSuffix(ROLES_DN_SUFFIX)
                    .agentDNSuffix(AGENT_DN_SUFFIX)
                    .groupDNSuffix(GROUP_DN_SUFFIX)
                    .addGroupMapping("/QA Group", "ou=QA,dc=jboss,dc=org")
                    .supportIdentityType(Agent.class)
                    .supportIdentityType(User.class)
=======
                    .addRealm(Realm.DEFAULT_REALM)
                    .addTier("Application A", "Application B", "Application C")
                    .baseDN(getBaseDn())
                    .bindDN(getBindDn())
                    .bindCredential(getBindCredential())
                    .url(getConnectionUrl())
                    .userDNSuffix(getUserDnSuffix())
                    .roleDNSuffix(getRolesDnSuffix())
                    .agentDNSuffix(getAgentDnSuffix())
                    .groupDNSuffix(getGroupDnSuffix())
                    .addGroupMapping("/QA Group", "ou=QA," + getBaseDn())
                    .supportFeature(FeatureGroup.agent)
                    .supportFeature(FeatureGroup.user)
>>>>>>> 14f502bb69a9449e55d3d17818efa3d8477d3310
                    .supportFeature(FeatureGroup.credential)
                    .supportFeature(FeatureGroup.attribute)
                .jpa()
                    .addRealm(Realm.DEFAULT_REALM)
                    .addTier("Application A", "Application B", "Application C")
                    .identityClass(IdentityObject.class)
                    .attributeClass(IdentityObjectAttribute.class)
                    .relationshipClass(RelationshipObject.class)
                    .relationshipIdentityClass(RelationshipIdentityWeakObject.class)
                    .relationshipAttributeClass(RelationshipObjectAttribute.class)
                    .partitionClass(PartitionObject.class)
                    .supportIdentityType(Role.class)
                    .supportIdentityType(Group.class)
                    .supportFeature(FeatureGroup.attribute)
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                .file()
                    .addRealm(Realm.DEFAULT_REALM)
                    .addTier("Application A", "Application B", "Application C")
                    .supportFeature(FeatureGroup.relationship)
                    .supportRelationshipType(CustomRelationship.class, Authorization.class);

        return new IdentityManagerFactory(builder.build());
    }

    @Override
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }
}
