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

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.TOTPCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * <p>
 * File based {@link org.picketlink.idm.spi.IdentityStore} implementation.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({PasswordCredentialHandler.class, X509CertificateCredentialHandler.class, DigestCredentialHandler.class, TOTPCredentialHandler.class})
public class FileIdentityStore implements PartitionStore<FileIdentityStoreConfiguration>, CredentialStore<FileIdentityStoreConfiguration> {

    private FileIdentityStoreConfiguration configuration;
    private FileDataSource fileDataSource;

    @Override
    public void setup(FileIdentityStoreConfiguration configuration) {
        this.configuration = configuration;
        this.fileDataSource = new FileDataSource(this.configuration);
    }

    @Override
    public void add(IdentityContext identityContext, Partition partition, String configurationName) {
        partition.setId(identityContext.getIdGenerator().generate());

        FilePartition filePartition = new FilePartition(partition, configurationName);

        this.fileDataSource.getPartitions().put(filePartition.getId(), filePartition);
        this.fileDataSource.flushPartitions();
    }

    @Override
    public void update(IdentityContext identityContext, Partition partition) {
        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        this.fileDataSource.getPartitions().put(partition.getId(),
                new FilePartition(partition, filePartition.getConfigurationName()));
        this.fileDataSource.flushPartitions();
    }

    @Override
    public void remove(IdentityContext identityContext, Partition partition) {
        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        this.fileDataSource.getPartitions().remove(filePartition.getId());

        //TODO: check for associated attributed types
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        P partition = null;

        try {
            partition = (P) resolve(partitionClass, name).getEntry();
        } catch (IdentityManagementException ime) {
            //just ignore if not found.
        }

        return partition;
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        for (FilePartition filePartition: this.fileDataSource.getPartitions().values()) {
            if (filePartition.getEntry().getClass().equals(partition.getClass())
                    && filePartition.getEntry().getName().equals(partition.getName())) {
                return filePartition.getConfigurationName();
            }
        }

        return null;
    }

    @Override
    public void add(IdentityContext context, AttributedType attributedType) {
        //TODO: add
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        //TODO: Implement storeCredential
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCurrentCredential
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCredentials
    }

    @Override
    public void update(IdentityContext context, AttributedType value) {
        //TODO: Implement update
    }

    @Override
    public void remove(IdentityContext context, AttributedType value) {
        //TODO: Implement remove
    }

    @Override
    public <I extends IdentityType> I getIdentity(Class<I> identityType, String id) {
        return null;  //TODO: Implement getIdentity
    }

    @Override
    public Agent getAgent(IdentityContext context, String loginName) {
        return null;  //TODO: Implement getAgent
    }

    @Override
    public User getUser(IdentityContext context, String loginName) {
        return null;  //TODO: Implement getUser
    }

    @Override
    public Group getGroup(IdentityContext context, String groupPath) {
        return null;  //TODO: Implement getGroup
    }

    @Override
    public Group getGroup(IdentityContext context, String name, Group parent) {
        return null;  //TODO: Implement getGroup
    }

    @Override
    public Role getRole(IdentityContext context, String name) {
        return null;  //TODO: Implement getRole
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        return Collections.emptyList();
    }

    @Override
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return null;  //TODO: Implement fetchQueryResults
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return 0;  //TODO: Implement countQueryResults
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType type, Attribute<? extends Serializable> attribute) {
        //TODO: Implement setAttribute
    }

    @Override
    public <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType type, String attributeName) {
        return null;  //TODO: Implement getAttribute
    }

    @Override
    public void removeAttribute(IdentityContext context, AttributedType type, String attributeName) {
        //TODO: Implement removeAttribute
    }

    @Override
    public void validateCredentials(IdentityContext context, Credentials credentials) {
        //TODO: Implement validateCredentials
    }

    @Override
    public void updateCredential(IdentityContext context, Account account, Object credential, Date effectiveDate, Date expiryDate) {
        //TODO: Implement updateCredential
    }

    @Override
    public FileIdentityStoreConfiguration getConfig() {
        return this.configuration;
    }

    /**
     * <p>Resolves the corresponding {@link FilePartition} for the given {@link Partition}.</p>
     *
     * @param partition
     * @return
     * @throws IdentityManagementException if no {@link FilePartition} exists for the given partition
     */
    private FilePartition resolve(Class<? extends Partition> type, String name) throws IdentityManagementException {
        for (FilePartition filePartition: this.fileDataSource.getPartitions().values()) {
            Partition storedPartition = filePartition.getEntry();

            if (storedPartition.getClass().equals(type) && storedPartition.getName().equals(name)) {
                return filePartition;
            }
        }

        throw MESSAGES.partitionNotFoundWithName(type, name);
    }
}