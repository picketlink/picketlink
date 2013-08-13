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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.picketlink.idm.model.Partition;

/**
 * @author pedroigor
 */
public class FilePartition extends AbstractFileAttributedType<Partition> {

    private static final long serialVersionUID = -8949732184464473476L;
    private static final String VERSION = "1";

    private transient String configurationName;
    private transient Map<String, Map<String, FileIdentityType>> identityTypes = new ConcurrentHashMap<String,
            Map<String,FileIdentityType>>();
    private transient Map<String, Map<String, List<FileCredentialStorage>>> credentials = new ConcurrentHashMap<String, Map<String, List<FileCredentialStorage>>>();

    protected FilePartition(Partition object, String configurationName) {
        super(VERSION, object);
        this.configurationName = configurationName;
    }

    @Override
    protected void doReadObject(ObjectInputStream s) throws Exception {
        super.doReadObject(s);

        this.configurationName = s.readObject().toString();
    }

    @Override
    protected void doWriteObject(ObjectOutputStream s) throws Exception {
        super.doWriteObject(s);

        s.writeObject(configurationName);
    }

    @Override
    protected Partition doCreateInstance(Map<String, Serializable> properties) throws Exception {
        String name = properties.get("name").toString();
        return (Partition) Class.forName(getType()).getConstructor(String.class).newInstance(name);
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);

        Partition partition = getEntry();

        properties.put("name", partition.getName());
    }

    @Override
    protected Partition doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        Partition partition = super.doPopulateEntry(properties);

        return partition;
    }

    public String getConfigurationName() {
        return this.configurationName;
    }

    public Map<String, Map<String, FileIdentityType>> getIdentityTypes() {
        return this.identityTypes;
    }

    public void setIdentityTypes(Map<String, Map<String, FileIdentityType>> identityTypes) {
        this.identityTypes = identityTypes;
    }

    public Map<String, Map<String, List<FileCredentialStorage>>> getCredentials() {
        return this.credentials;
    }

    public void setCredentials(Map<String, Map<String, List<FileCredentialStorage>>> credentials) {
        this.credentials = credentials;
    }
}
