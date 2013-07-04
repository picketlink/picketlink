package org.picketlink.test.idm.suites;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.picketlink.idm.PartitionManager;
import org.picketlink.test.idm.IdentityManagerRunner;
import org.picketlink.test.idm.TestLifecycle;

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
        return null;
    }

    @Override
    public void onDestroy() {
        this.entityManager.getTransaction().commit();
        this.entityManager.close();
        this.emf.close();
    }
}
