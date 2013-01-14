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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.PartitionStoreConfiguration;
import org.picketlink.idm.model.Partition;

/**
 * @author Pedro Silva
 *
 */
public class FilePartitionStoreConfiguration extends PartitionStoreConfiguration {
    
    private String workingDir;
    
    private File partitionsFile;
    private Map<String, Partition> partitions = new HashMap<String, Partition>();

    private boolean alwaysCreateFiles;
    
    @Override
    public void init() throws SecurityConfigurationException {
        if (getWorkingDir() == null) {
            setWorkingDir(System.getProperty("java.io.tmpdir"));
        }
        
        initDataFiles();
    }
    
    /**
     * <p>
     * Initializes the files used to store the informations.
     * </p>
     */
    private void initDataFiles() {
        File workingDirectoryFile = initWorkingDirectory();

        this.partitionsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-partitions.db"));
    }
    
    /**
     * <p>
     * Initializes the working directory.
     * </p>
     * 
     * @return
     */
    private File initWorkingDirectory() {
        String workingDir = getWorkingDir();

        File workingDirectoryFile = new File(workingDir);

        if (!workingDirectoryFile.exists()) {
            workingDirectoryFile.mkdirs();
        }

        return workingDirectoryFile;
    }
    
    /**
     * <p>
     * Check if the specified {@link File} exists. If not create it.
     * </p>
     * 
     * @param file
     * @return
     */
    private File checkAndCreateFile(File file) {
        if (isAlwaysCreateFiles() && file.exists()) {
            file.delete();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
        }

        return file;
    }
    
    public String getWorkingDir() {
        return workingDir;
    }
    
    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }
    
    public boolean isAlwaysCreateFiles() {
        return this.alwaysCreateFiles;
    }
    
    public Map<String, Partition> getPartitions() {
        return this.partitions;
    }
    
    public File getPartitionsFile() {
        return this.partitionsFile;
    }
}
