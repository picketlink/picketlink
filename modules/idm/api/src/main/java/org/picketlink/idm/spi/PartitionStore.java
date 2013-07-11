package org.picketlink.idm.spi;

import org.picketlink.idm.model.Partition;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface PartitionStore {
    void createPartition(SecurityContext context, Partition partition);
    Partition findPartition(SecurityContext context, String id);

    void removePartition(SecurityContext context, Partition partition);
}
