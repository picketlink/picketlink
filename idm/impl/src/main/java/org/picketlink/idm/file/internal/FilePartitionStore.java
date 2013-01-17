/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.file.internal;

import static org.picketlink.idm.file.internal.FileUtils.delete;

import java.io.File;
import java.util.Collection;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * @author Pedro Silva
 * 
 */
public class FilePartitionStore implements PartitionStore<FilePartitionStoreConfiguration> {

    private FilePartitionStoreConfiguration config;
    private IdentityStoreInvocationContext context;

    @Override
    public void createPartition(Partition partition) {
        if (Realm.class.isInstance(partition)) {
            if (getRealm(partition.getName()) != null) {
                throw new IdentityManagementException("A Realm with name [" + partition.getName() + "] already exists.");
            }
        }

        if (Tier.class.isInstance(partition)) {
            if (getTier(partition.getName()) != null) {
                throw new IdentityManagementException("A Tier with name [" + partition.getName() + "] already exists.");
            }
        }

        partition.setId(this.context.getIdGenerator().generate());

        FilePartition filePartition = getConfig().getDataSource().initPartition(partition.getId());

        filePartition.setPartition(partition);

        getConfig().getPartitions().put(partition.getId(), filePartition);
        getConfig().getDataSource().flushPartitions();
    }

    @Override
    public void removePartition(Partition partition) {
        String id = partition.getId();

        if (id == null) {
            throw new IdentityManagementException("No identifier provided.");
        }

        if (getConfig().getPartitions().containsKey(partition.getId())) {
            FilePartition filePartition = getConfig().getPartitions().get(partition.getId());

            if (!filePartition.getAgents().isEmpty() || !filePartition.getRoles().isEmpty()
                    || !filePartition.getGroups().isEmpty()) {
                throw new IdentityManagementException(
                        "Realm could not be removed. There IdentityTypes associated with it. Remove them first.");
            }

            delete(new File(getConfig().getDataSource().getWorkingDir() + File.separator + partition.getId()));
            getConfig().getPartitions().remove(partition.getId());
            getConfig().getDataSource().flushPartitions();
        } else {
            throw new IdentityManagementException("No Partition found with the given id [" + id + "].");
        }
    }

    @Override
    public Realm getRealm(String name) {
        Collection<FilePartition> partitions = getConfig().getPartitions().values();

        for (FilePartition partition : partitions) {
            if (Realm.class.isInstance(partition.getPartition())) {
                Realm realm = (Realm) partition.getPartition();

                if (realm.getName().equals(name)) {
                    return realm;
                }
            }
        }

        return null;
    }

    @Override
    public Tier getTier(String name) {
        Collection<FilePartition> partitions = getConfig().getPartitions().values();

        for (FilePartition partition : partitions) {
            if (Tier.class.isInstance(partition.getPartition())) {
                Tier tier = (Tier) partition.getPartition();

                if (tier.getName().equals(name)) {
                    return tier;
                }
            }
        }

        return null;
    }

    @Override
    public void setup(FilePartitionStoreConfiguration config, IdentityStoreInvocationContext context) {
        this.config = config;
        this.context = context;
    }

    private FilePartitionStoreConfiguration getConfig() {
        return this.config;
    }

}
