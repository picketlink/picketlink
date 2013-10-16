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
package org.picketlink.identity.federation.core.sts.registry;

import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * {@code FileBasedRevocationRegistry} is a revocation registry implementation that uses a file to store the ids of the
 * revoked
 * (canceled) security tokens. By default all ids are stored in $HOME/picketlink-store/sts/revoked.ids but a different
 * location
 * can be specified through the constructor that takes the file name as a parameter.
 * </p>
 * <p>
 * NOTE: this implementation use a local cache to avoid reading the file system every time a revocation check is made,
 * making
 * this registry a bad choice for distributed scenarios. Even though the registry file is updated whenever a new id is
 * revoked,
 * each node in the cluster will have its own cached view and thus a token that has been canceled by one node may be
 * accepted by
 * another live node as the caches are not refreshed or synchronized.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class FileBasedRevocationRegistry extends FileBasedSTSOperations implements RevocationRegistry {

    protected static final String FILE_NAME = "revoked.ids";

    // this set contains the ids of the revoked security tokens.
    protected static Set<String> revokedIds = new HashSet<String>();

    // the file that stores the revoked ids.
    protected File registryFile;

    /**
     * <p>
     * Creates an instance of {@code RevocationRegistryFile} that stores the canceled ids in the default
     * {@code $HOME/picketlink-store/sts/revoked.ids} file.
     * </p>
     */
    public FileBasedRevocationRegistry() {
        this(FILE_NAME);
    }

    /**
     * <p>
     * Creates an instance of {@code RevocationRegistryFile} that stores the canceled ids in specified file.
     * </p>
     *
     * @param registryFile a {@code String} that indicates the file that must be used to store revoked ids.
     */
    public FileBasedRevocationRegistry(String registryFileName) {
        super();
        this.registryFile = create(registryFileName);

        // load the revoked ids cache.
        this.loadRevokedIds();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#isRevoked(java.lang.String,
     * java.lang.String)
     */
    public boolean isRevoked(String tokenType, String id) {
        return revokedIds.contains(id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#revokeToken(java.lang.String,
     * java.lang.String)
     */
    public synchronized void revokeToken(String tokenType, String id) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(PicketLinkCoreSTS.rte);

        try {
            // write a new line with the revoked id at the end of the file.
            BufferedWriter writer = new BufferedWriter(new FileWriter(this.registryFile, true));
            writer.write(id + "\n");
            writer.close();
        } catch (IOException ioe) {
            logger.debug("Error appending content to registry file: " + ioe.getMessage());
            ioe.printStackTrace();
        }
        // add the revoked id to the local cache.
        revokedIds.add(id);
    }

    /**
     * <p>
     * This method loads the ids of the revoked assertions from the registry file. All retrieved ids are set in the
     * local cache
     * of revoked ids.
     * </p>
     */
    private void loadRevokedIds() {
        try {
            // read the file contents and populate the local cache.
            BufferedReader reader = new BufferedReader(new FileReader(this.registryFile));
            String id = reader.readLine();
            while (id != null) {
                revokedIds.add(id);
                id = reader.readLine();
            }
            reader.close();
        } catch (IOException ioe) {
            logger.debug("Error opening registry file: " + ioe.getMessage());
            ioe.printStackTrace();
        }
    }
}