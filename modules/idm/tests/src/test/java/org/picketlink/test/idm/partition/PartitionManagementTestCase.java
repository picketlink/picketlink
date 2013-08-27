package org.picketlink.test.idm.partition;

import org.junit.Test;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.basic.Realm;
import org.picketlink.idm.model.basic.Tier;
import org.picketlink.test.idm.AbstractPartitionManagerTestCase;
import org.picketlink.test.idm.Configuration;
import org.picketlink.test.idm.testers.FileStoreConfigurationTester;
import org.picketlink.test.idm.testers.IdentityConfigurationTester;
import org.picketlink.test.idm.testers.JPAStoreConfigurationTester;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author  Pedro Igor
 */
@Configuration(include= {JPAStoreConfigurationTester.class, FileStoreConfigurationTester.class})
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
}
