package org.picketlink.idm.event;

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.Partition;

/**
 * <p>This event is raised whenever a new {@link org.picketlink.idm.model.Partition} is created.</p>
 *
 * @author Pedro Igor
 */
public class PartitionCreatedEvent extends AbstractBaseEvent {

    private final Partition partition;

    public PartitionCreatedEvent(Partition partition, PartitionManager partitionManager) {
        super(partitionManager);
        this.partition = partition;
    }

    public Partition getPartition() {
        return this.partition;
    }
}
