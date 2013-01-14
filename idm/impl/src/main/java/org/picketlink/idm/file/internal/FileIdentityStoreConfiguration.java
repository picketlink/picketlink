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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Role;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class FileIdentityStoreConfiguration extends IdentityStoreConfiguration {

    private String workingDir;
    
    /**
     * <p> Indicates that the files must be always recreated during the initialization. </p> 
     */
    private boolean alwaysCreateFiles = true;

    /**
     * Defines the feature set for this IdentityStore
     */
    private FeatureSet featureSet = new FeatureSet();
    
    private File agentsFile;
    private File rolesFile;

    private File groupsFile;
    private File relationshipsFile;
    private File credentialsFile;
    
    private Map<String, Agent> agents = new HashMap<String, Agent>();
    private Map<String, Role> roles = new HashMap<String, Role>();
    private Map<String, Group> groups = new HashMap<String, Group>();
    private Map<String, Map<String, List<FileCredentialStorage>>> credentials = new HashMap<String, Map<String, List<FileCredentialStorage>>>();
    private Map<String, List<FileRelationshipStorage>> relationships = new HashMap<String, List<FileRelationshipStorage>>();

    @Override
    public void init() throws SecurityConfigurationException {
        this.featureSet.addSupportedFeature(Feature.all);

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

        this.agentsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-agents.db"));
        this.rolesFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-roles.db"));
        this.groupsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-groups.db"));
        this.relationshipsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-memberships.db"));
        this.credentialsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-credentials.db"));
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
    
    /**
     * <p>
     * Initializes the store.
     * </p>
     */
    private void initialize() {
        loadUsers();
        loadRoles();
        loadGroups();
        loadMemberships();
    }

    /**
     * <p>
     * Load all persisted groups from the filesystem.
     * </p>
     */
    private void loadGroups() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(getGroupsFile());
            ois = new ObjectInputStream(fis);

            this.groups = (Map<String, Group>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Load all persisted memberships from the filesystem.
     * </p>
     */
    private void loadMemberships() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(getMembershipsFile());
            ois = new ObjectInputStream(fis);

            this.relationships = (Map<String, List<FileRelationshipStorage>>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Load all persisted roles from the filesystem.
     * </p>
     */
    private void loadRoles() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(getRolesFile());
            ois = new ObjectInputStream(fis);

            this.roles = (Map<String, Role>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * <p>
     * Load all persisted users from the filesystem.
     * </p>
     */
    private void loadUsers() {
        ObjectInputStream ois = null;

        try {
            FileInputStream fis = new FileInputStream(getAgentsFile());
            ois = new ObjectInputStream(fis);

            this.agents = (Map<String, Agent>) ois.readObject();
        } catch (Exception e) {
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
    }
    
    @Override
    public FeatureSet getFeatureSet() {
        return this.featureSet;
    }

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public boolean isAlwaysCreateFiles() {
        return alwaysCreateFiles;
    }

    public void setAlwaysCreateFiles(boolean alwaysCreateFiles) {
        this.alwaysCreateFiles = alwaysCreateFiles;
    }
    
    public File getAgentsFile() {
        return this.agentsFile;
    }
    
    public File getRolesFile() {
        return this.rolesFile;
    }
    
    public File getGroupsFile() {
        return this.groupsFile;
    }
    
    public File getMembershipsFile() {
        return this.relationshipsFile;
    }
    
    public File getCredentialsFile() {
        return this.credentialsFile;
    }
    
    public Map<String, Agent> getAgents() {
        return this.agents;
    }
    
    public Map<String, Role> getRoles() {
        return this.roles;
    }
    
    public Map<String, List<FileRelationshipStorage>> getRelationships() {
        return this.relationships;
    }
    
    public Map<String, Map<String, List<FileCredentialStorage>>> getCredentials() {
        return this.credentials;
    }
    
    public Map<String, Group> getGroups() {
        return this.groups;
    }
    
    private String getDefaultTmpDir() {
        return System.getProperty("java.io.tmpdir", "/tmp");
    }

}
