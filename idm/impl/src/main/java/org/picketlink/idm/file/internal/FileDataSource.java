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

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.file.internal.FileUtils.createFileIfNotExists;
import static org.picketlink.idm.file.internal.FileUtils.delete;
import static org.picketlink.idm.file.internal.FileUtils.readObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;

/**
 * @author Pedro Silva
 *
 */
public class FileDataSource {

    /**
     * <p>
     * Default value for the thread pool size when <code>asyncWrite</code> is enabled.
     * </p>
     */
    private static final int ASYNC_FLUSH_THREAD_POOL = 5;

    /**
     * <p>
     * Default buffer length when flushing changes to the filesystem. The higher the value greater will be the throughput.
     * </p>
     */
    private static final int FLUSH_BYTE_BUFFER = 1024;

    private static final String DEFAULT_WORKING_DIR = System.getProperty("java.io.tmpdir", File.separator + "tmp")
            + File.separator + "pl-idm";

    private static final String GROUPS_FILE_NAME = "pl-idm-groups.db";
    private static final String CREDENTIALS_FILE_NAME = "pl-idm-credentials.db";
    private static final String RELATIONSHIPS_FILE_NAME = "pl-idm-relationships.db";
    private static final String PARTITIONS_FILE_NAME = "pl-idm-partitions.db";
    private static final String ROLES_FILE_NAME = "pl-idm-roles.db";
    private static final String AGENTS_FILE_NAME = "pl-idm-agents.db";

    private String workingDir = DEFAULT_WORKING_DIR;

    /**
     * <p>
     * Indicates that the files must be always recreated during the initialization.
     * </p>
     */
    private boolean alwaysCreateFiles = true;

    /**
     * <p>
     * Indicates whether changes should be flushed in a asynchronous fashion.
     * </p>
     */
    private boolean asyncWrite;

    /**
     * <p>
     * If <code>asyncWrite</code> is true, tells the size of the thread pool used to flush changes to the filesystem.
     * </p>
     */
    private int asyncThreadPool = ASYNC_FLUSH_THREAD_POOL;

    /**
     * <p>
     * Holds all configured {@link FilePartition} instances loaded from the filesystem. This {@link Map} is also used to persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, FilePartition> partitions = new ConcurrentHashMap<String, FilePartition>();

    private Map<String, List<FileRelationship>> relationships = new ConcurrentHashMap<String, List<FileRelationship>>();

    private boolean initialized;

    private ExecutorService executorService;

    public void init(FileIdentityStoreConfiguration config) {
        this.alwaysCreateFiles = config.isAlwaysCreateFiles();
        this.asyncThreadPool = config.getAsyncThreadPool();
        this.asyncWrite = config.isAsyncWrite();
        this.workingDir = config.getWorkingDir();

        init();
    }

    /**
     * <p>
     * Initializes the working directory.
     * </p>
     *
     * @return
     */
    private void initWorkingDirectory() {
        String workingDir = getWorkingDir();

        File workingDirectoryFile = new File(workingDir);

        if (workingDirectoryFile.exists()) {
            if (isAlwaysCreateFiles()) {
                LOGGER.fileConfigAlwaysCreateWorkingDir(workingDirectoryFile.getPath());
                delete(workingDirectoryFile);
            }
        }

        workingDirectoryFile.mkdirs();

        LOGGER.fileConfigUsingWorkingDir(workingDirectoryFile.getPath());
    }

    private void init() {
        if (!this.initialized) {
            initWorkingDirectory();

            File partitionsFile = FileUtils.createFileIfNotExists(new File(getWorkingDir() + File.separator
                    + PARTITIONS_FILE_NAME));

            loadPartitions(partitionsFile);

            File relationshipsFile = FileUtils.createFileIfNotExists(new File(getWorkingDir() + File.separator
                    + RELATIONSHIPS_FILE_NAME));

            this.relationships = FileUtils.readObject(relationshipsFile);

            if (this.relationships == null) {
                this.relationships = new HashMap<String, List<FileRelationship>>();
            }

            LOGGER.debug("Loaded Relationships");

            if (this.asyncWrite) {
                LOGGER.debugf("Async write enabled. Using thread pool of size %s", this.asyncThreadPool);
                this.executorService = Executors.newFixedThreadPool(this.asyncThreadPool);
            }

            this.initialized = true;
        }
    }

    private void loadPartitions(File partitionsFile) {
        this.partitions = FileUtils.readObject(partitionsFile);

        if (this.partitions == null) {
            LOGGER.debugf("No partitions to load from %s", partitionsFile.getPath());
            this.partitions = new ConcurrentHashMap<String, FilePartition>();
        } else {
            LOGGER.infof("Loading %s Partitions from %s", this.partitions.size(), partitionsFile.getPath());

            Set<Entry<String, FilePartition>> entrySet = this.partitions.entrySet();

            for (Entry<String, FilePartition> entry : entrySet) {
                initPartition(entry.getKey());
            }
        }
    }

    protected void initPartition(String partitionId) {
        FilePartition filePartition = this.partitions.get(partitionId);

        LOGGER.debugf("Initializing Partition [%s] with id [%s].", filePartition.getId(), partitionId);

        String agentsPath = getWorkingDir() + File.separator + partitionId + File.separator + AGENTS_FILE_NAME;

        File agentsFile = createFileIfNotExists(new File(agentsPath));

        Map<String, FileAgent> agents = readObject(agentsFile);

        if (agents == null) {
            agents = new ConcurrentHashMap<String, FileAgent>();
        }

        filePartition.setAgents(agents);

        LOGGER.debugf("Loaded Agents for Partition [%s].", filePartition.getId());

        String rolesPath = getWorkingDir() + File.separator + partitionId + File.separator + ROLES_FILE_NAME;

        File rolesFile = createFileIfNotExists(new File(rolesPath));

        Map<String, FileRole> roles = readObject(rolesFile);

        if (roles == null) {
            roles = new HashMap<String, FileRole>();
        }

        filePartition.setRoles(roles);

        LOGGER.debugf("Loaded Roles for Partition [%s].", filePartition.getId());

        String groupsPath = getWorkingDir() + File.separator + partitionId + File.separator + GROUPS_FILE_NAME;

        File groupsFile = createFileIfNotExists(new File(groupsPath));

        Map<String, FileGroup> groups = readObject(groupsFile);

        if (groups == null) {
            groups = new HashMap<String, FileGroup>();
        }

        filePartition.setGroups(groups);

        LOGGER.debugf("Loaded Groups for Partition [%s].", filePartition.getId());

        String credentialsPath = getWorkingDir() + File.separator + partitionId + File.separator + CREDENTIALS_FILE_NAME;

        File credentialsFile = createFileIfNotExists(new File(credentialsPath));

        Map<String, Map<String, List<FileCredentialStorage>>> credentials = readObject(credentialsFile);

        if (credentials == null) {
            credentials = new HashMap<String, Map<String, List<FileCredentialStorage>>>();
        }

        filePartition.setCredentials(credentials);

        LOGGER.debugf("Loaded Credentials for Partition [%s].", filePartition.getId());
    }

    protected void flushAgents(FilePartition partition) {
        flush(partition, AGENTS_FILE_NAME, partition.getAgents());
    }

    protected void flushRoles(FilePartition partition) {
        flush(partition, ROLES_FILE_NAME, partition.getRoles());
    }

    protected void flushGroups(FilePartition partition) {
        flush(partition, GROUPS_FILE_NAME, partition.getGroups());
    }

    protected void flushPartitions() {
        flush(PARTITIONS_FILE_NAME, getPartitions());
    }

    protected void flushCredentials(Realm realm) {
        FilePartition filePartition = getPartitions().get(realm.getId());
        flush(filePartition, CREDENTIALS_FILE_NAME, filePartition.getCredentials());
    }

    protected void flushRelationships() {
        flush(RELATIONSHIPS_FILE_NAME, getRelationships());
    }

    private void flush(final FilePartition partition, final String fileName, final Object object) {
        flush(partition.getId() + File.separator + fileName, object);
    }

    private void flush(final String fileName, final Object object) {
        if (this.asyncWrite) {
            this.executorService.execute(new Runnable() {

                @Override
                public void run() {
                    performFlush(fileName, object);
                }
            });
        } else {
            performFlush(fileName, object);
        }
    }

    private void performFlush(final String fileName, final Object object) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;

        try {
            String filePath = getWorkingDir() + File.separator + fileName;
            fos = new FileOutputStream(filePath);

            FileChannel channel = fos.getChannel();

            bos = new ByteArrayOutputStream(FLUSH_BYTE_BUFFER);

            oos = new ObjectOutputStream(bos);

            oos.writeObject(object);

            channel.write(ByteBuffer.wrap(bos.toByteArray()));
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing changes to file system.", e);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {

            }
            try {
                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {

            }
            try {
                if (bos != null) {
                    bos.close();
                }
            } catch (Exception e) {

            }
        }
    }

    protected Map<String, Agent> getAgents(Partition partition) {
        Map<String, FileAgent> fileAgents = getPartition(partition).getAgents();

        Map<String, Agent> agents = new ConcurrentHashMap<String, Agent>();

        for (Entry<String, FileAgent> fileAgent : fileAgents.entrySet()) {
            agents.put(fileAgent.getKey(), fileAgent.getValue().getEntry());
        }

        return agents;
    }

    protected FilePartition getPartition(Partition partition) {
        if (!this.partitions.containsKey(partition.getId())) {
            this.partitions.put(partition.getId(), new FilePartition(partition));
            initPartition(partition.getId());
            flushPartitions();
        }

        return this.partitions.get(partition.getId());
    }

    protected Map<String, Role> getRoles(Partition partition) {
        Map<String, FileRole> fileRoles = getPartition(partition).getRoles();

        Map<String, Role> roles = new HashMap<String, Role>();

        for (Entry<String, FileRole> fileRole : fileRoles.entrySet()) {
            roles.put(fileRole.getKey(), fileRole.getValue().getEntry());
        }

        return roles;
    }

    protected Map<String, List<FileRelationship>> getRelationships() {
        return this.relationships;
    }

    protected Map<String, Map<String, List<FileCredentialStorage>>> getCredentials(Realm realm) {
        return getPartition(realm).getCredentials();
    }

    protected Map<String, Group> getGroups(Partition partition) {
        Map<String, FileGroup> fileGroups = getPartition(partition).getGroups();

        Map<String, Group> groups = new HashMap<String, Group>();

        for (Entry<String, FileGroup> fileRole : fileGroups.entrySet()) {
            groups.put(fileRole.getKey(), fileRole.getValue().getEntry());
        }

        return groups;
    }

    public Map<String, FilePartition> getPartitions() {
        return this.partitions;
    }

    public String getWorkingDir() {
        if (this.workingDir == null) {
            this.workingDir = DEFAULT_WORKING_DIR;
        }

        return this.workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public boolean isAlwaysCreateFiles() {
        return this.alwaysCreateFiles;
    }

    public void setAlwaysCreateFiles(boolean alwaysCreateFiles) {
        this.alwaysCreateFiles = alwaysCreateFiles;
    }

    public void setAsyncWrite(boolean asyncWrite) {
        this.asyncWrite = asyncWrite;
    }

    public boolean isAsyncWrite() {
        return this.asyncWrite;
    }

    public void setAsyncThreadPool(int asyncThreadPool) {
        this.asyncThreadPool = asyncThreadPool;
    }

    public int getAsyncThreadPool() {
        return this.asyncThreadPool;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void close() {
        this.initialized = false;
    }
}
