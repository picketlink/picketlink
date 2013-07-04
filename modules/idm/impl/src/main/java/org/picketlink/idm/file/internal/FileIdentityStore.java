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
import java.util.Date;
import java.util.List;
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

        this.fileDataSource.getPartitions().put(partition.getId(), filePartition);
        this.fileDataSource.flushPartitions();
    }

    @Override
    public void add(IdentityContext context, AttributedType value) {
        System.out.println(value);
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        for (FilePartition filePartition: this.fileDataSource.getPartitions().values()) {
            Partition partition = filePartition.getEntry();

            if (partition.getClass().equals(partitionClass) && partition.getName().equals(name)) {
                return (P) partition;
            }
        }

        return null;
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
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        //TODO: Implement storeCredential
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCurrentCredential
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Agent agent, Class<T> storageClass) {
        return null;  //TODO: Implement retrieveCredentials
    }

    @Override
    public void update(IdentityContext identityContext, Partition partition) {
        //TODO: Implement update
    }

    @Override
    public void remove(IdentityContext identityContext, Partition partition) {
        //TODO: Implement remove
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
        return null;  //TODO: Implement fetchQueryResults
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
    public void updateCredential(IdentityContext context, Agent agent, Object credential, Date effectiveDate, Date expiryDate) {
        //TODO: Implement updateCredential
    }

    @Override
    public FileIdentityStoreConfiguration getConfig() {
        return this.configuration;
    }

}