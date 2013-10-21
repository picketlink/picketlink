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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.file.internal.FileUtils.createFileIfNotExists;
import static org.picketlink.idm.file.internal.FileUtils.delete;
import static org.picketlink.idm.file.internal.FileUtils.readObject;
import static org.picketlink.idm.IDMInternalLog.FILE_STORE_LOGGER;

/**
 * @author Pedro Silva
 */
public class FileDataSource {

    /**
     * <p>
     * Default buffer length when flushing changes to the filesystem. The higher the value greater will be the
     * throughput.
     * </p>
     */
    private static final int FLUSH_BYTE_BUFFER = 1024;

    private static final String DEFAULT_WORKING_DIR = System.getProperty("java.io.tmpdir", File.separator + "tmp")
            + File.separator + "pl-idm";

    private static final String PARTITIONS_FILE_NAME = "pl-idm-partitions.db";
    private static final String IDENTITY_TYPES__FILE_NAME = "pl-idm-identity-types.db";
    private static final String ATTRIBUTED_TYPES__FILE_NAME = "pl-idm-attributed-types.db";
    private static final String ATTRIBUTES_FILE_NAME = "pl-idm-attributes.db";
    private static final String RELATIONSHIPS_FILE_NAME = "pl-idm-relationships.db";
    private static final String CREDENTIALS_FILE_NAME = "pl-idm-credentials.db";

    private final FileIdentityStoreConfiguration configuration;

    /**
     * <p>
     * Holds all stored {@link FilePartition} instances loaded from the filesystem. This {@link Map} is also used to
     * persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, FilePartition> partitions;

    /**
     * <p>
     * Holds all stored {@link FileRelationship} instances loaded from the filesystem. This {@link Map} is also used to
     * persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, Map<String, FileRelationship>> relationships;

    /**
     * <p>
     * Holds all stored {@link FileAttribute} instances loaded from the filesystem. This {@link Map} is also used to
     * persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, FileAttribute> attributes;

    /**
     * <p>
     * Holds all stored {@link FileAttributedType} instances loaded from the filesystem. This {@link Map} is also used
     * to persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, FileAttributedType> attributedTypes;

    private ExecutorService executorService;

    FileDataSource(FileIdentityStoreConfiguration configuration) {
        this.configuration = configuration;
        init();
    }

    Map<String, FilePartition> getPartitions() {
        return this.partitions;
    }

    Map<String, Map<String, FileRelationship>> getRelationships() {
        return this.relationships;
    }

    Map<String, FileAttribute> getAttributes() {
        return attributes;
    }

    public Map<String, FileAttributedType> getAttributedTypes() {
        return this.attributedTypes;
    }

    void flushPartitions() {
        flush(PARTITIONS_FILE_NAME, getPartitions());
    }

    void flushPartitions(FilePartition partition) {
        initPartition(partition.getId());
        flush(PARTITIONS_FILE_NAME, getPartitions());
    }

    void flushAttributedTypes(FilePartition partition) {
        flush(partition, IDENTITY_TYPES__FILE_NAME, partition.getIdentityTypes());
    }

    void flushRelationships() {
        flush(RELATIONSHIPS_FILE_NAME, getRelationships());
    }

    void flushAttributes() {
        flush(ATTRIBUTES_FILE_NAME, getAttributes());
    }

    void flushAttributedTypes() {
        flush(ATTRIBUTED_TYPES__FILE_NAME, getAttributedTypes());
    }

    void flushCredentials(FilePartition partition) {
        FilePartition filePartition = getPartitions().get(partition.getId());
        flush(filePartition, CREDENTIALS_FILE_NAME, filePartition.getCredentials());
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
            if (this.configuration.isAlwaysCreateFiles()) {
                FILE_STORE_LOGGER.fileConfigAlwaysCreateWorkingDir(workingDirectoryFile.getPath());
                delete(workingDirectoryFile);
            }
        }

        workingDirectoryFile.mkdirs();

        FILE_STORE_LOGGER.fileConfigUsingWorkingDir(workingDirectoryFile.getPath());
    }

    private void init() {
        initWorkingDirectory();

        File partitionsFile =
                createFileIfNotExists(getWorkingDirFile(PARTITIONS_FILE_NAME));

        loadPartitions(partitionsFile);

        Map<String, Map<String, FileRelationship>> relationships =
                readObject(createFileIfNotExists(getWorkingDirFile(RELATIONSHIPS_FILE_NAME)));

        if (relationships == null) {
            relationships = new ConcurrentHashMap<String, Map<String, FileRelationship>>();
        }

        this.relationships = relationships;

        Map<String, FileAttribute> attributes =
                readObject(createFileIfNotExists(getWorkingDirFile(ATTRIBUTES_FILE_NAME)));

        if (attributes == null) {
            attributes = new ConcurrentHashMap<String, FileAttribute>();
        }

        this.attributes = attributes;

        Map<String, FileAttributedType> attrubtedTypes =
                readObject(createFileIfNotExists(getWorkingDirFile(ATTRIBUTED_TYPES__FILE_NAME)));

        if (attrubtedTypes == null) {
            attrubtedTypes = new ConcurrentHashMap<String, FileAttributedType>();
        }

        this.attributedTypes = attrubtedTypes;

        if (this.configuration.isAsyncWrite()) {
            FILE_STORE_LOGGER.fileAsyncWriteEnabled(this.configuration.getAsyncThreadPool());
            this.executorService = Executors.newFixedThreadPool(this.configuration.getAsyncThreadPool());
        }
    }

    private void loadPartitions(File partitionsFile) {
        this.partitions = readObject(partitionsFile);

        if (this.partitions == null) {
            if (isDebugEnabled()) {
                FILE_STORE_LOGGER.debugf("No partitions to load from %s", partitionsFile.getPath());
            }
            this.partitions = new ConcurrentHashMap<String, FilePartition>();
        } else {
            if (isDebugEnabled()) {
                FILE_STORE_LOGGER.debugf("Loading [%s] Partition(s) from %s", this.partitions.size(), partitionsFile.getPath());
            }

            Set<Entry<String, FilePartition>> entrySet = this.partitions.entrySet();

            for (Entry<String, FilePartition> entry : entrySet) {
                initPartition(entry.getKey());
            }
        }
    }

    private void initPartition(String partitionId) {
        FilePartition filePartition = this.partitions.get(partitionId);

        if (isDebugEnabled()) {
            FILE_STORE_LOGGER.debugf("Initializing Partition [%s] with id [%s].", filePartition.getEntry().getName(), partitionId);
        }

        File agentsFile = createFileIfNotExists(getWorkingDirFile(partitionId + File.separator + IDENTITY_TYPES__FILE_NAME));

        Map<String, Map<String, FileIdentityType>> identityTypes = readObject(agentsFile);

        if (identityTypes == null) {
            identityTypes = new ConcurrentHashMap<String, Map<String, FileIdentityType>>();
        }

        filePartition.setIdentityTypes(identityTypes);

        if (isDebugEnabled()) {
            FILE_STORE_LOGGER.debugf("Loaded Identity Types [%s] for Partition [%s].", filePartition.getIdentityTypes().size(), filePartition.getId());
        }

        File credentialsFile = createFileIfNotExists(getWorkingDirFile(partitionId + File.separator + CREDENTIALS_FILE_NAME));

        Map<String, Map<String, List<FileCredentialStorage>>> credentials = readObject(credentialsFile);

        if (credentials == null) {
            credentials = new ConcurrentHashMap<String, Map<String, List<FileCredentialStorage>>>();
        }

        filePartition.setCredentials(credentials);

        if (isDebugEnabled()) {
            FILE_STORE_LOGGER.debugf("Loaded Credentials [%s] for Partition [%s].", filePartition.getCredentials().size(), filePartition.getId());
        }
    }

    private String getWorkingDir() {
        String workingDir = this.configuration.getWorkingDir();

        if (isNullOrEmpty(workingDir)) {
            workingDir = DEFAULT_WORKING_DIR;
        }

        return workingDir;
    }

    private void flush(final FilePartition partition, final String fileName, final Object object) {
        flush(partition.getId() + File.separator + fileName, object);
    }

    private void flush(final String fileName, final Object object) {
        if (this.configuration.isAsyncWrite()) {
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

    private synchronized void performFlush(final String fileName, final Object object) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(getWorkingDir() + File.separator + fileName, "rw");

            FileChannel channel = randomAccessFile.getChannel();

            bos = new ByteArrayOutputStream(FLUSH_BYTE_BUFFER);

            oos = new ObjectOutputStream(bos);

            oos.writeObject(object);

            channel.write(ByteBuffer.wrap(bos.toByteArray()));
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing changes to file system.", e);
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {

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

    private File getWorkingDirFile(String name) {
        return new File(getWorkingDir() + File.separator + name);
    }

    private boolean isDebugEnabled() {
        return FILE_STORE_LOGGER.isDebugEnabled();
    }

}