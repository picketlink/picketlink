/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
