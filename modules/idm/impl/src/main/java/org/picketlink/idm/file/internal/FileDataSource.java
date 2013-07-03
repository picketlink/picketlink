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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.file.internal.FileUtils.delete;

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

    private final FileIdentityStoreConfiguration configuration;

    /**
     * <p>
     * Holds all configured {@link FilePartition} instances loaded from the filesystem. This {@link Map} is also used to persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, FilePartition> partitions = new ConcurrentHashMap<String, FilePartition>();

    private ExecutorService executorService;

    public FileDataSource(FileIdentityStoreConfiguration configuration) {
        this.configuration = configuration;
        init();
    }

    public Map<String, FilePartition> getPartitions() {
        return this.partitions;
    }

    void flushPartitions() {
        flush(PARTITIONS_FILE_NAME, getPartitions());
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
                LOGGER.fileConfigAlwaysCreateWorkingDir(workingDirectoryFile.getPath());
                delete(workingDirectoryFile);
            }
        }

        workingDirectoryFile.mkdirs();

        LOGGER.fileConfigUsingWorkingDir(workingDirectoryFile.getPath());
    }

    private void init() {
        initWorkingDirectory();

        File partitionsFile = FileUtils
                .createFileIfNotExists(new File(getWorkingDir() + File.separator + PARTITIONS_FILE_NAME));

        loadPartitions(partitionsFile);

        if (this.configuration.isAsyncWrite()) {
            LOGGER.debugf("Async write enabled. Using thread pool of size %s", this.configuration.getAsyncThreadPool());
            this.executorService = Executors.newFixedThreadPool(this.configuration.getAsyncThreadPool());
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

    private void initPartition(String partitionId) {
    }

    private String getWorkingDir() {
        return this.configuration.getWorkingDir();
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
}