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
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Tier;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;
import org.picketlink.idm.spi.PartitionStore;

/**
 * <p>
 * {@link PartitionStore} implementation that persists {@link Partition} instances using the {@link FileBasedIdentityStore}.
 * </p>
 * 
 * @author Pedro Silva
 * 
 */
public class FilePartitionStore implements PartitionStore {

    private FileBasedIdentityStore identityStore;

    public FilePartitionStore(FileBasedIdentityStore identityStore) {
        this.identityStore = identityStore;

        if (getRealm(Realm.DEFAULT_REALM) == null) {
            createDefaultRealm();
        }
    }

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

        partition.setId(getContext().getIdGenerator().generate());

        FilePartition filePartition = new FilePartition(partition);

        getDataSource().getPartitions().put(filePartition.getId(), filePartition);

        getDataSource().initPartition(partition.getId());

        getPartitions().put(partition.getId(), filePartition);
        getDataSource().flushPartitions();
    }

    @Override
    public void removePartition(Partition partition) {
        String id = partition.getId();

        if (getPartitions().containsKey(partition.getId())) {
            FilePartition filePartition = getDataSource().getPartition(partition.getId());

            if (!filePartition.getAgents().isEmpty() || !filePartition.getRoles().isEmpty()
                    || !filePartition.getGroups().isEmpty()) {
                throw new IdentityManagementException(
                        "Realm could not be removed. There IdentityTypes associated with it. Remove them first.");
            }

            delete(new File(getDataSource().getWorkingDir() + File.separator + partition.getId()));
            getPartitions().remove(partition.getId());
            getDataSource().flushPartitions();
        } else {
            throw new IdentityManagementException("No Partition found with the given id [" + id + "].");
        }
    }

    @Override
    public Realm getRealm(String name) {
        Collection<FilePartition> partitions = getPartitions().values();
        Realm realm = null;

        for (FilePartition partition : partitions) {
            if (Realm.class.isInstance(partition.getPartition())) {
                if (partition.getPartition().getName().equals(name)) {
                    realm = (Realm) partition.getPartition();
                    break;
                }
            }
        }

        return realm;
    }

    private void createDefaultRealm() {
        createPartition(new Realm(Realm.DEFAULT_REALM));
    }

    @Override
    public Tier getTier(String name) {
        Collection<FilePartition> partitions = getPartitions().values();

        for (FilePartition partition : partitions) {
            if (Tier.class.isInstance(partition.getPartition())) {
                Tier tier = (Tier) partition.getPartition();

                if (tier.getName().equals(name)) {
                    if (tier.getParent() != null) {
                        // during the unmarshalling the parent tier is only a reference to the real entry. We need to load the
                        // real unmarshalled parent entry.
                        tier.setParent(getTier(tier.getParent().getName()));
                    }

                    return tier;
                }
            }
        }

        return null;
    }

    private FileIdentityStoreConfiguration getConfig() {
        return this.identityStore.getConfig();
    }

    private IdentityStoreInvocationContext getContext() {
        return this.identityStore.getContext();
    }

    private FileDataSource getDataSource() {
        return getConfig().getDataSource();
    }

    private Map<String, FilePartition> getPartitions() {
        return getDataSource().getPartitions();
    }
}
