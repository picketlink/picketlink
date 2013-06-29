package org.picketlink.test.idm.suites;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Authorization;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
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
import static org.junit.runners.Suite.SuiteClasses;

/**
 * <p>
 * Test suite for the {@link IdentityManager} using a {@link JPAIdentityStore} in conjunction with a {@link LDAPIdentityStore}.
 * This suite tests a common scenario where the JPAis used to store only agents, users and credentials and the File store for
 * roles, groups and relationships.
 * </p>
 * 
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
@RunWith(IdentityManagerRunner.class)
@SuiteClasses({ RelationshipQueryTestCase.class, CustomRelationshipTestCase.class, PasswordCredentialTestCase.class,
        UserManagementTestCase.class, RoleManagementTestCase.class, GroupManagementTestCase.class,
        AgentManagementTestCase.class, AgentQueryTestCase.class, UserQueryTestCase.class, RoleQueryTestCase.class,
        GroupQueryTestCase.class, AgentGroupRoleRelationshipTestCase.class, AgentGroupsRelationshipTestCase.class,
        UserGrantRelationshipTestCase.class, AgentGrantRelationshipTestCase.class, GroupGrantRelationshipTestCase.class,
        UserGroupRoleRelationshipTestCase.class, GroupMembershipTestCase.class })
public class JPAUsersFileRolesGroupsRelationshipTestSuite implements TestLifecycle {

    private static JPAUsersFileRolesGroupsRelationshipTestSuite instance;

    private EntityManagerFactory emf;
    private EntityManager entityManager;

    public static TestLifecycle init() throws Exception {
        if (instance == null) {
            instance = new JPAUsersFileRolesGroupsRelationshipTestSuite();
        }

        return instance;
    }

    @BeforeClass
    public static void onBeforeClass() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void onDestroyClass() {
    }

    @Override
    public void onInit() {
        this.emf = Persistence.createEntityManagerFactory("jpa-identity-store-tests-pu");
        this.entityManager = emf.createEntityManager();
        this.entityManager.getTransaction().begin();
    }

    @SuppressWarnings("unchecked")
    @Override
    public PartitionManager createPartitionManager() {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        builder
            .stores()
                .jpa()
                    .identityClass(IdentityObject.class)
                    .credentialClass(CredentialObject.class)
                    .credentialAttributeClass(CredentialObjectAttribute.class)
                    .attributeClass(IdentityObjectAttribute.class)
                    .partitionClass(PartitionObject.class)
                    .supportType(Agent.class)
                    .supportType(User.class)
                    .supportCredentials(true)
//                    .supportFeature(FeatureGroup.attribute)
                    .addContextInitializer(new JPAContextInitializer(emf) {
                        @Override
                        public EntityManager getEntityManager() {
                            return entityManager;
                        }
                    })
                .file()
                    .supportType(Group.class)
                    .supportType(Role.class)
//                    .supportFeature(FeatureGroup.attribute)
//                    .supportFeature(FeatureGroup.relationship)
                    .supportType(CustomRelationship.class, Authorization.class);

        return null;
//        return new IdentityManagerFactory(builder.build());
    }

    @Override
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }
}
