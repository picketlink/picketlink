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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.model.Partition;

/**
 * @author Pedro Silva
 *
 */
public class FilePartition extends AbstractFileEntry<Partition> {

    private static final long serialVersionUID = 1L;

    private static final transient String FILE_PARTITION_VERSION = "1";

    private transient Map<String, FileAgent> agents = new ConcurrentHashMap<String, FileAgent>();
    private transient Map<String, FileRole> roles = new ConcurrentHashMap<String, FileRole>();
    private transient Map<String, FileGroup> groups = new ConcurrentHashMap<String, FileGroup>();
    private transient Map<String, Map<String, List<FileCredentialStorage>>> credentials = new ConcurrentHashMap<String, Map<String, List<FileCredentialStorage>>>();

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

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        Partition partition = getPartition();

        properties.put("id", partition.getId());
    }

    @Override
    protected Partition doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        String id = properties.get("id").toString();

        Partition partition = (Partition) Class.forName(getType()).getConstructor(String.class).newInstance(id);

        return partition;
    }

    @Override
    public String toString() {
        return getId();
    }
}
