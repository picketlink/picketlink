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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.spi.IdentityStore.Feature;

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
    private Set<Feature> featureSet = new HashSet<Feature>();
    
    private File usersFile;
    private File rolesFile = new File(getDefaultTmpDir() + "/pl-idm-work/pl-idm-roles.db");

    private File groupsFile = new File(getDefaultTmpDir() + "/pl-idm-work/pl-idm-groups.db");
    private File membershipsFile = new File(getDefaultTmpDir() + "/pl-idm-work/pl-idm-memberships.db");
    private File credentialsFile = new File(getDefaultTmpDir() + "/pl-idm-work/pl-idm-credentials.db");
    
    private Map<String, Agent> users = new HashMap<String, Agent>();
    private Map<String, Role> roles = new HashMap<String, Role>();
    private Map<String, Group> groups = new HashMap<String, Group>();
    private List<GroupRole> memberships = new ArrayList<GroupRole>();
    private Map<String, Map<String, List<FileCredentialStorage>>> credentials = new HashMap<String, Map<String, List<FileCredentialStorage>>>();

    @Override
    public void init() throws SecurityConfigurationException {
        this.featureSet.add(Feature.all);

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

        this.usersFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-users.db"));
        this.rolesFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-roles.db"));
        this.groupsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-groups.db"));
        this.membershipsFile = checkAndCreateFile(new File(workingDirectoryFile.getPath() + "/pl-idm-memberships.db"));
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

            this.memberships = (List<GroupRole>) ois.readObject();
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
            FileInputStream fis = new FileInputStream(getUsersFile());
            ois = new ObjectInputStream(fis);

            this.users = (Map<String, Agent>) ois.readObject();
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
    public Set<Feature> getFeatureSet() {
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
    
    public File getUsersFile() {
        return this.usersFile;
    }
    
    public File getRolesFile() {
        return this.rolesFile;
    }
    
    public File getGroupsFile() {
        return this.groupsFile;
    }
    
    public File getMembershipsFile() {
        return this.membershipsFile;
    }
    
    public File getCredentialsFile() {
        return this.credentialsFile;
    }
    
    public Map<String, Agent> getUsers() {
        return this.users;
    }
    
    public Map<String, Role> getRoles() {
        return this.roles;
    }
    
    public List<GroupRole> getMemberships() {
        return this.memberships;
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
