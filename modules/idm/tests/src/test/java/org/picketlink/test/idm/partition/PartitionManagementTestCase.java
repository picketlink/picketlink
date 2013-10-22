package org.picketlink.test.idm.partition;

import org.junit.Test;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.Tier;
import org.picketlink.idm.model.basic.User;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;
import org.picketlink.test.idm.testers.LDAPUserGroupJPARoleConfigurationTester;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author  Pedro Igor
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class, LDAPUserGroupJPARoleConfigurationTester.class})
public class PartitionManagementTestCase extends AbstractPartitionManagerTestCase  {

    public PartitionManagementTestCase(final IdentityConfigurationTester visitor) {
        super(visitor);
    }

    @Test
    public void testGetAllPartitions() {
        PartitionManager partitionManager = getPartitionManager();

        Realm realmA = new Realm("Realm A");
        Realm realmB = new Realm("Realm B");

        partitionManager.add(realmA);
        partitionManager.add(realmB);

        Tier tierA = new Tier("Tier A");
        Tier tierB = new Tier("Tier B");
        Tier tierC = new Tier("Tier C");

        partitionManager.add(tierA);
        partitionManager.add(tierB);
        partitionManager.add(tierC);

        List<Partition> partitions = partitionManager.getPartitions(Partition.class);

        assertFalse(partitions.isEmpty());

        // 6 because we have the default partition
        assertEquals(6, partitions.size());
    }

    @Test
    public void testLookupById() {
        PartitionManager partitionManager = getPartitionManager();

        Realm realmA = new Realm("Realm A");

        partitionManager.add(realmA);

        Tier tierA = new Tier("Tier A");

        partitionManager.add(tierA);

        Realm lookedUpRealmA = partitionManager.lookupById(Realm.class, realmA.getId());

        assertNotNull(lookedUpRealmA.getId());
        assertEquals(realmA.getId(), lookedUpRealmA.getId());

        Tier lookedUpTierA = partitionManager.lookupById(Tier.class, tierA.getId());

        assertNotNull(lookedUpTierA.getId());
        assertEquals(tierA.getId(), lookedUpTierA.getId());

        assertNull(partitionManager.lookupById(Tier.class, realmA.getId()));
    }

    @Test
    @Configuration (exclude = {LDAPUserGroupJPARoleConfigurationTester.class})
    public void testRemovePartitionWithIdentityTypes() {
        PartitionManager partitionManager = getPartitionManager();
        Realm somePartition = new Realm("somePartition");

        partitionManager.add(somePartition);

        IdentityManager identityManager = partitionManager.createIdentityManager(somePartition);

        User userA = new User("userA");

        identityManager.add(userA);

        User userB = new User("userB");

        identityManager.add(userB);

        User userC = new User("userC");

        identityManager.add(userC);

        assertNotNull(BasicModel.getUser(identityManager, userA.getLoginName()));
        assertNotNull(BasicModel.getUser(identityManager, userB.getLoginName()));
        assertNotNull(BasicModel.getUser(identityManager, userC.getLoginName()));

        Role roleA = new Role("roleA");

        identityManager.add(roleA);

        Role roleB = new Role("roleB");

        identityManager.add(roleB);

        Role roleC = new Role("roleC");

        identityManager.add(roleC);

        assertNotNull(BasicModel.getRole(identityManager, roleA.getName()));
        assertNotNull(BasicModel.getRole(identityManager, roleB.getName()));
        assertNotNull(BasicModel.getRole(identityManager, roleC.getName()));

        RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

        BasicModel.grantRole(relationshipManager, userA, roleA);
        BasicModel.grantRole(relationshipManager, userB, roleB);
        BasicModel.grantRole(relationshipManager, userC, roleC);

        assertTrue(BasicModel.hasRole(relationshipManager, userA, roleA));
        assertTrue(BasicModel.hasRole(relationshipManager, userB, roleB));
        assertTrue(BasicModel.hasRole(relationshipManager, userC, roleC));

        partitionManager.remove(somePartition);

        new Realm("somePartition");

        partitionManager.add(somePartition);

        identityManager = partitionManager.createIdentityManager(somePartition);

        assertNull(BasicModel.getUser(identityManager, userA.getLoginName()));
        assertNull(BasicModel.getUser(identityManager, userB.getLoginName()));
        assertNull(BasicModel.getUser(identityManager, userC.getLoginName()));
    }

    @Test
    public void testRemovePartitionWithRoles() {
        PartitionManager partitionManager = getPartitionManager();
        Realm somePartition = new Realm("somePartition");

        partitionManager.add(somePartition);

        IdentityManager identityManager = partitionManager.createIdentityManager(somePartition);

        User userA = new User("userA");

        identityManager.add(userA);

        User userB = new User("userB");

        identityManager.add(userB);

        User userC = new User("userC");

        identityManager.add(userC);

        assertNotNull(BasicModel.getUser(identityManager, userA.getLoginName()));
        assertNotNull(BasicModel.getUser(identityManager, userB.getLoginName()));
        assertNotNull(BasicModel.getUser(identityManager, userC.getLoginName()));

        Role roleA = new Role("roleA");

        identityManager.add(roleA);

        Role roleB = new Role("roleB");

        identityManager.add(roleB);

        Role roleC = new Role("roleC");

        identityManager.add(roleC);

        assertNotNull(BasicModel.getRole(identityManager, roleA.getName()));
        assertNotNull(BasicModel.getRole(identityManager, roleB.getName()));
        assertNotNull(BasicModel.getRole(identityManager, roleC.getName()));

        RelationshipManager relationshipManager = partitionManager.createRelationshipManager();

        BasicModel.grantRole(relationshipManager, userA, roleA);
        BasicModel.grantRole(relationshipManager, userB, roleB);
        BasicModel.grantRole(relationshipManager, userC, roleC);

        assertTrue(BasicModel.hasRole(relationshipManager, userA, roleA));
        assertTrue(BasicModel.hasRole(relationshipManager, userB, roleB));
        assertTrue(BasicModel.hasRole(relationshipManager, userC, roleC));

        partitionManager.remove(somePartition);

        partitionManager.add(somePartition);

        identityManager = partitionManager.createIdentityManager(somePartition);

        assertNull(BasicModel.getRole(identityManager, roleA.getName()));
        assertNull(BasicModel.getRole(identityManager, roleB.getName()));
        assertNull(BasicModel.getRole(identityManager, roleC.getName()));
    }

}
