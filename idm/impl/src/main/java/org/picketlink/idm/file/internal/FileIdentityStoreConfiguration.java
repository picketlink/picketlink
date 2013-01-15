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

import static org.picketlink.idm.file.internal.FileUtils.createFile;
import static org.picketlink.idm.file.internal.FileUtils.delete;
import static org.picketlink.idm.file.internal.FileUtils.readObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityStoreInvocationContext;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class FileIdentityStoreConfiguration extends IdentityStoreConfiguration {

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
     * Defines the feature set for this {@link IdentityStore}.
     * </p>
     */
    private FeatureSet featureSet = new FeatureSet();

    /**
     * <p>
     * Holds all configured {@link FilePartition} instances loaded from the filesystem. This {@link Map} is also used to persist
     * information to the filesystem.
     * </p>
     */
    private Map<String, FilePartition> partitions = new HashMap<String, FilePartition>();

    @Override
    public void init() throws SecurityConfigurationException {
        configureFeatureSet();
        initWorkingDirectory();
    }

    @Override
    public FeatureSet getFeatureSet() {
        return this.featureSet;
    }

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
        Realm realm = context.getRealm();
        String realmId = getRealmId(realm);

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = new FilePartition();

            String filePath = getWorkingDir() + File.separator + realmId + File.separator + AGENTS_FILE_NAME;

            File agentsFile = createFile(new File(filePath));

            Map<String, Agent> load = readObject(agentsFile);

            if (load != null) {
                partition.setAgents(load);
            }

            this.partitions.put(realmId, partition);
            flushAgents(realm);
        }

        return partition.getAgents();
    }

    Map<String, Role> getRoles(IdentityStoreInvocationContext context) {
        Realm realm = context.getRealm();
        String realmId = getRealmId(realm);

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = new FilePartition();

            String filePath = getWorkingDir() + File.separator + realmId + File.separator + ROLES_FILE_NAME;

            File agentsFile = createFile(new File(filePath));

            Map<String, Agent> load = readObject(agentsFile);

            if (load != null) {
                partition.setAgents(load);
            }

            this.partitions.put(realmId, partition);
            flushAgents(realm);
        }

        return partition.getRoles();
    }

    Map<String, List<FileRelationshipStorage>> getRelationships(IdentityStoreInvocationContext context) {
        Realm realm = context.getRealm();
        String realmId = getRealmId(realm);

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = new FilePartition();

            String filePath = getWorkingDir() + File.separator + realmId + File.separator + RELATIONSHIPS_FILE_NAME;

            File agentsFile = createFile(new File(filePath));

            Map<String, Agent> load = readObject(agentsFile);

            if (load != null) {
                partition.setAgents(load);
            }

            this.partitions.put(realmId, partition);
            flushAgents(realm);
        }

        return partition.getRelationships();
    }

    Map<String, Map<String, List<FileCredentialStorage>>> getCredentials(IdentityStoreInvocationContext context) {
        Realm realm = context.getRealm();
        String realmId = getRealmId(realm);

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = new FilePartition();

            String filePath = getWorkingDir() + File.separator + realmId + File.separator + CREDENTIALS_FILE_NAME;

            File agentsFile = createFile(new File(filePath));

            Map<String, Agent> load = readObject(agentsFile);

            if (load != null) {
                partition.setAgents(load);
            }

            this.partitions.put(realmId, partition);
            flushAgents(realm);
        }

        return partition.getCredentials();
    }

    Map<String, Group> getGroups(IdentityStoreInvocationContext context) {
        Realm realm = context.getRealm();
        String realmId = getRealmId(realm);

        FilePartition partition = this.partitions.get(realmId);

        if (partition == null) {
            partition = new FilePartition();

            String filePath = getWorkingDir() + File.separator + realmId + File.separator + GROUPS_FILE_NAME;

            File agentsFile = createFile(new File(filePath));

            Map<String, Agent> load = readObject(agentsFile);

            if (load != null) {
                partition.setAgents(load);
            }

            this.partitions.put(realmId, partition);
            flushAgents(realm);
        }

        return partition.getGroups();
    }

    void flushAgents(Realm realm) {
        flush(realm, AGENTS_FILE_NAME, this.partitions.get(getRealmId(realm)).getAgents());
    }

    void flushRoles(Realm realm) {
        flush(realm, ROLES_FILE_NAME, this.partitions.get(getRealmId(realm)).getRoles());
    }

    void flushGroups(Realm realm) {
        flush(realm, GROUPS_FILE_NAME, this.partitions.get(getRealmId(realm)).getGroups());
    }

    void flushCredentials(Realm realm) {
        flush(realm, CREDENTIALS_FILE_NAME, this.partitions.get(getRealmId(realm)).getCredentials());
    }

    void flushRelationships(Realm realm) {
        flush(realm, RELATIONSHIPS_FILE_NAME, this.partitions.get(getRealmId(realm)).getRelationships());
    }

    void flush(Realm realm, String fileName, Object object) {
        try {
            FileOutputStream fos = new FileOutputStream(getWorkingDir() + File.separator + getRealmId(realm) + File.separator + fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            oos.close();
        } catch (Exception e) {
            throw new IdentityManagementException("Error flushing changes to file system.", e);
        }
    }

    /**
     * <p>
     * Configures the {@link Feature} set supported by this store.
     * </p>
     */
    private void configureFeatureSet() {
        this.featureSet.addSupportedFeature(Feature.all);
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
                delete(workingDirectoryFile);
            }
        }

        workingDirectoryFile.mkdirs();
    }

    /**
     * <p>
     * Returns the identifier for the given {@link Realm}. If it is null, the default {@link Realm} identifier will be returned.
     * </p>
     * 
     * @param realm
     * @return
     */
    private String getRealmId(Realm realm) {
        String realmId = Realm.DEFAULT_REALM;

        if (realm != null) {
            realmId = realm.getId();
        }
        return realmId;
    }
}
