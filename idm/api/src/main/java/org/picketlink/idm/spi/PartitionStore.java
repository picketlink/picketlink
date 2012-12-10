package org.picketlink.idm.spi;

import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;

/**
 * Defines the methods for general management of Partitions, in addition to specific
 * methods for Realms and Tiers
 * 
 * @author Shane Bryzak
 *
 */
public interface PartitionStore {
    /**
     * 
     * @param tier
     */
    void createPartition(Partition partition);

    /**
     * 
     * @param tier
     */
    void removePartition(Partition partition);

    /**
     * 
     * @param realmName
     * @return
     */
    Realm getRealm(String realmName);

    /**
     * 
     * @param tierId
     * @return
     */
    Tier getTier(String tierId);
}
