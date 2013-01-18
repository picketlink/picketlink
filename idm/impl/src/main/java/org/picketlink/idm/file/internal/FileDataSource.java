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

import static org.picketlink.idm.file.internal.FileUtils.createFileIfNotExists;
import static org.picketlink.idm.file.internal.FileUtils.delete;
import static org.picketlink.idm.file.internal.FileUtils.readObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * @author Pedro Silva
 *
 */
public class FileDataSource {

    private static final String DEFAULT_WORKING_DIR = System.getProperty("java.io.tmpdir", File.separator + "tmp")
            + File.separator + "pl-idm";

    private static final String GROUPS_FILE_NAME = "pl-idm-groups.db";

    private static final String CREDENTIALS_FILE_NAME = "pl-idm-credentials.db";

    private static final String RELATIONSHIPS_FILE_NAME = "pl-idm-relationships.db";

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
     * Holds all configured {@link FilePartition} instances loaded from the filesystem. This {@link Map} is also used to persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, FilePartition> partitions = new HashMap<String, FilePartition>();

    private boolean initialized;
    
    private File partitionsFile;
    
    public String getWorkingDir() {
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

    Map<String, Agent> getAgents(IdentityStoreInvocationContext context) {
        String realmId = Realm.DEFAULT_REALM;

        if (context.getRealm() != null) {
            realmId = context.getRealm().getId();
        }

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = initPartition(realmId);
        }

        return partition.getAgents();
    }

    Map<String, Role> getRoles(String realmId) {
        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = initPartition(realmId);
        }

        return partition.getRoles();
    }

    Map<String, List<FileRelationshipStorage>> getRelationships(IdentityStoreInvocationContext context) {
        String realmId = context.getPartition().getId();

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = initPartition(realmId);
        }

        return partition.getRelationships();
    }

    public FilePartition initPartition(String realmId) {
        FilePartition partition = new FilePartition();

        String relationshipFilePath = getWorkingDir() + File.separator + realmId + File.separator + RELATIONSHIPS_FILE_NAME;
        
        File relationshipFile = createFileIfNotExists(new File(relationshipFilePath));

        Map<String, List<FileRelationshipStorage>> relationships = readObject(relationshipFile);

        if (relationships != null) {
            partition.setRelationships(relationships);
        }

        String agentsPath = getWorkingDir() + File.separator + realmId + File.separator + AGENTS_FILE_NAME;
        
        File agentsFile = createFileIfNotExists(new File(agentsPath));

        Map<String, Agent> agents = readObject(agentsFile);

        if (agents != null) {
            partition.setAgents(agents);
        }

        String rolesPath = getWorkingDir() + File.separator + realmId + File.separator + ROLES_FILE_NAME;
        
        File rolesFile = createFileIfNotExists(new File(rolesPath));

        Map<String, Role> roles = readObject(rolesFile);

        if (roles != null) {
            partition.setRoles(roles);
        }
        
        String groupsPath = getWorkingDir() + File.separator + realmId + File.separator + GROUPS_FILE_NAME;
        
        File groupsFile = createFileIfNotExists(new File(groupsPath));

        Map<String, Group> groups = readObject(groupsFile);

        if (groups != null) {
            partition.setGroups(groups);
        }

        String credentialsPath = getWorkingDir() + File.separator + realmId + File.separator + CREDENTIALS_FILE_NAME;
        
        File credentialsFile = createFileIfNotExists(new File(credentialsPath));

        Map<String, Map<String, List<FileCredentialStorage>>> credentials = readObject(credentialsFile);

        if (credentials != null) {
            partition.setCredentials(credentials);
        }

        this.partitions.put(realmId, partition);
        
        return partition;
    }

    Map<String, Map<String, List<FileCredentialStorage>>> getCredentials(IdentityStoreInvocationContext context) {
        String realmId = context.getPartition().getId();

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = initPartition(realmId);
        }

        return partition.getCredentials();
    }

    Map<String, Group> getGroups(String realmId) {
        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = initPartition(realmId);
        }

        return partition.getGroups();
    }

    void flushAgents(IdentityStoreInvocationContext context) {
        String realmId = Realm.DEFAULT_REALM;

        if (context.getRealm() != null) {
            realmId = context.getRealm().getId();
        }
        
        flush(realmId, AGENTS_FILE_NAME, this.partitions.get(realmId).getAgents());
    }
    
    synchronized void flushPartitions() {
        try {
            FileOutputStream fos = new FileOutputStream(this.partitionsFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(getPartitions());
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing partitions changes to file system.", e);
        }
    }

    void flushRoles(IdentityStoreInvocationContext context) {
        flush(context, ROLES_FILE_NAME, this.partitions.get(context.getPartition().getId()).getRoles());
    }

    void flushGroups(IdentityStoreInvocationContext context) {
        flush(context, GROUPS_FILE_NAME, this.partitions.get(context.getPartition().getId()).getGroups());
    }

    void flushCredentials(IdentityStoreInvocationContext context) {
        flush(context, CREDENTIALS_FILE_NAME, this.partitions.get(context.getPartition().getId()).getCredentials());
    }

    void flushRelationships(IdentityStoreInvocationContext context) {
        flush(context, RELATIONSHIPS_FILE_NAME, this.partitions.get(context.getPartition().getId()).getRelationships());
    }

    void flush(IdentityStoreInvocationContext context, String fileName, Object object) {
        try {
            String filePath = getWorkingDir() + File.separator + context.getPartition().getId() + File.separator + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing changes to file system.", e);
        }
    }
    
    void flush(String realmId, String fileName, Object object) {
        try {
            FileOutputStream fos = new FileOutputStream(getWorkingDir() + File.separator + realmId + File.separator + fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing changes to file system.", e);
        }
    }

    /**
     * <p>
     * Initializes the working directory.
     * </p>
     * 
     * @return
     */
    void initWorkingDirectory() {
        String workingDir = getWorkingDir();

        File workingDirectoryFile = new File(workingDir);

        if (workingDirectoryFile.exists()) {
            if (isAlwaysCreateFiles()) {
                delete(workingDirectoryFile);
            }
        }

        workingDirectoryFile.mkdirs();
        
        this.partitionsFile = FileUtils.createFileIfNotExists(new File(workingDirectoryFile.getPath() + "/pl-idm-partitions.db"));
    }



    public void init() {
        if (!this.initialized) {
            initWorkingDirectory();
            this.initialized = true;
        }
    }
    
    public Map<String, FilePartition> getPartitions() {
        return this.partitions;
    }

    public Map<String, Agent> getAgents(String realmId) {
        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = initPartition(realmId);
        }

        return partition.getAgents();
    }
}
