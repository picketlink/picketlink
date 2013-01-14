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

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
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
        partition.setId(this.context.getIdGenerator().generate());
        getConfig().getPartitions().put(partition.getName(), partition);
        flushPartitions();
    }

    @Override
    public void removePartition(Partition partition) {
        String id = partition.getName();
        
        if (id == null) {
            throw new IdentityManagementException("No identifier provided.");
        }
        
        if (getConfig().getPartitions().containsKey(id)) {
            getConfig().getPartitions().remove(id);
            flushPartitions();
        } else {
            throw new IdentityManagementException("No Partition found with the given id [" + id + "].");
        }
    }

    @Override
    public Realm getRealm(String name) {
        Collection<Partition> partitions = getConfig().getPartitions().values();
        
        for (Partition partition : partitions) {
            if (Realm.class.isInstance(partition)) {
                Realm realm = (Realm) partition;
                
                if (realm.getName().equals(name)) {
                    return realm;
                }
            }
        }
        
        return null;
    }

    @Override
    public Tier getTier(String name) {
        Collection<Partition> partitions = getConfig().getPartitions().values();
        
        for (Partition partition : partitions) {
            if (Tier.class.isInstance(partition)) {
                Tier tier = (Tier) partition;
                
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
    
    /**
     * <p>
     * Flush all changes made to agents to the filesystem.
     * </p>
     */
    synchronized void flushPartitions() {
        try {
            FileOutputStream fos = new FileOutputStream(this.getConfig().getPartitionsFile());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getConfig().getPartitions());
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing partitions changes to file system.", e);
        }
    }
}
