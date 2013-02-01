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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Tier;

/**
 * @author Pedro Silva
 * 
 */
public class FilePartition extends AbstractFileEntry<Partition> {

    private static final long serialVersionUID = 1L;

    private static final transient String FILE_PARTITION_VERSION = "1";

    private transient Map<String, FileAgent> agents = new HashMap<String, FileAgent>();
    private transient Map<String, FileRole> roles = new HashMap<String, FileRole>();
    private transient Map<String, FileGroup> groups = new HashMap<String, FileGroup>();
    private transient Map<String, Map<String, List<FileCredentialStorage>>> credentials = new HashMap<String, Map<String, List<FileCredentialStorage>>>();

    public FilePartition(Partition partition) {
        super(FILE_PARTITION_VERSION, partition);
    }

    public Map<String, FileAgent> getAgents() {
        return agents;
    }

    public void setAgents(Map<String, FileAgent> agents) {
        this.agents = agents;
    }

    public Map<String, FileRole> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, FileRole> roles) {
        this.roles = roles;
    }

    public Map<String, FileGroup> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, FileGroup> groups) {
        this.groups = groups;
    }

    public Map<String, Map<String, List<FileCredentialStorage>>> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, Map<String, List<FileCredentialStorage>>> credentials) {
        this.credentials = credentials;
    }

    public Partition getPartition() {
        return (Partition) super.getEntry();
    }

    public String getId() {
        return getPartition().getId();
    }

    public String getName() {
        return getPartition().getName();
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        Partition partition = getPartition();
        
        properties.put("id", partition.getId());
        properties.put("name", partition.getName());

        if (Tier.class.isInstance(partition)) {
            Tier tier = (Tier) partition;

            if (tier.getParent() != null) {
                properties.put("parentId", tier.getParent().getId());
                properties.put("parentName", tier.getParent().getName());
            }
        }
    }

    @Override
    protected Partition doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        String id = properties.get("id").toString();
        String name = properties.get("name").toString();

        Partition partition = (Partition) Class.forName(getType()).getConstructor(String.class).newInstance(name);

        partition.setId(id);
        partition.setName(name);

        if (Tier.class.isInstance(partition)) {
            Tier tier = (Tier) partition;

            Object parentName = properties.get("parentName");

            if (parentName != null) {
                Tier parentTier = new Tier(parentName.toString());

                parentTier.setId(properties.get("parentId").toString());

                tier.setParent(parentTier);
            }
        }
        
        return partition;
    }
}
