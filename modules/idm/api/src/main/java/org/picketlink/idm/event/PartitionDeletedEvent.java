package org.picketlink.idm.event;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;

/**
 * <p>This event is raised whenever a new {@link org.picketlink.idm.model.Partition} is deleted.</p>
 *
 * @author Pedro Igor
 */
public class PartitionDeletedEvent extends AbstractBaseEvent {

    private final Partition partition;

    public PartitionDeletedEvent(Partition partition, PartitionManager partitionManager) {
        super(partitionManager);
        this.partition = partition;
    }

    public Partition getPartition() {
        return this.partition;
    }
}
