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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
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
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.sample.Agent;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.User;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.internal.DefaultIdentityQuery;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;
import static java.util.Map.Entry;
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

        FilePartition filePartition = new FilePartition(cloneAttributedType(identityContext, partition), configurationName);

        this.fileDataSource.getPartitions().put(filePartition.getId(), filePartition);
        this.fileDataSource.flushPartitions(filePartition);
    }

    @Override
    public void update(IdentityContext identityContext, Partition partition) {
        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        this.fileDataSource.getPartitions().put(partition.getId(),
                new FilePartition(cloneAttributedType(identityContext, partition), filePartition.getConfigurationName()));
        this.fileDataSource.flushPartitions();
    }

    @Override
    public void remove(IdentityContext identityContext, Partition partition) {
        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        this.fileDataSource.getPartitions().remove(filePartition.getId());
        this.fileDataSource.flushPartitions();
    }

    @Override
    public <P extends Partition> P get(IdentityContext identityContext, Class<P> partitionClass, String name) {
        try {
            return (P) resolve(partitionClass, name).getEntry();
        } catch (IdentityManagementException ime) {
            //just ignore if not found.
        }

        return null;
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        return resolve(partition.getClass(), partition.getName()).getConfigurationName();
    }

    @Override
    public void add(IdentityContext context, final AttributedType attributedType) {
        FilePartition filePartition = resolve(context.getPartition().getClass(), context.getPartition().getName());

        attributedType.setId(context.getIdGenerator().generate());

        AttributedType newAttributedType = cloneAttributedType(context, attributedType);

        if (IdentityType.class.isInstance(newAttributedType)) {
            filePartition.getAttributedTypes().put(newAttributedType.getId(), new FileIdentityType((IdentityType) newAttributedType));
        } else {
            filePartition.getAttributedTypes().put(newAttributedType.getId(), new FileAttributedType(newAttributedType));
        }

        this.fileDataSource.flushAttributedTypes(filePartition);
    }

    @Override
    public void update(IdentityContext context, final AttributedType attributedType) {
        FilePartition filePartition = resolve(context.getPartition().getClass(), context.getPartition().getName());

        AttributedType updatedAttributedType = cloneAttributedType(context, (attributedType));

        if (IdentityType.class.isInstance(attributedType)) {
            filePartition.getAttributedTypes().put(attributedType.getId(), new FileIdentityType((IdentityType) updatedAttributedType));
        } else {
            filePartition.getAttributedTypes().put(attributedType.getId(), new FileAttributedType(updatedAttributedType));
        }

        this.fileDataSource.flushAttributedTypes(filePartition);
    }

    @Override
    public void remove(IdentityContext context, AttributedType value) {
        resolve(context.getPartition().getClass(), context.getPartition().getName()).getAttributedTypes().remove(value.getId());
    }

    @Override
    public User getUser(IdentityContext context, String loginName) {
        List<User> users = fetchQueryResults(context, new DefaultIdentityQuery<User>(context, User.class, this));

        if (users.isEmpty()) {
            return null;
        }

        return users.get(0);
    }

    @Override
    public <I extends IdentityType> I getIdentity(Class<I> identityType, String id) {
        return null; //TODO: Implement storeCredential
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
    public Agent getAgent(IdentityContext context, String loginName) {
        return null;  //TODO: Implement getAgent
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
        Object[] partitionParameters = identityQuery.getParameter(IdentityType.PARTITION);
        Partition partition = null;

        if (partitionParameters == null) {
            partitionParameters = new Object[]{context.getPartition()};
        }

        for (Object parameter : partitionParameters) {
            partition = (Partition) parameter;
        }

        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        List<V> result = new ArrayList<V>();

        Object[] ids = identityQuery.getParameter(IdentityType.ID);

        // if we have a ID parameter just get the an instance directly
        if (ids != null && ids.length > 0) {
            AbstractFileAttributedType fileAttributedType = filePartition.getAttributedTypes().get(ids[0]);

            if (fileAttributedType != null) {
                result.add(cloneAttributedType(context, (V) fileAttributedType.getEntry()));
            }
        } else {
            FileIdentityQueryHelper queryHelper = new FileIdentityQueryHelper(identityQuery, this);

            for (AbstractFileAttributedType fileAttributedType : filePartition.getAttributedTypes().values()) {
                IdentityType storedEntry = (IdentityType) fileAttributedType.getEntry();

                if (!IdentityType.class.isInstance(storedEntry)) {
                    continue;
                }

                if (!identityQuery.getIdentityType().isAssignableFrom(storedEntry.getClass())) {
                    continue;
                }

                boolean found = true;

                for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters(AttributeParameter.class).entrySet()) {
                    // if one of the parameters didn`t match is because the entry should not be selected
                    if (!found) {
                        break;
                    }

                    found = false;

                    QueryParameter queryParameter = entry.getKey();

                    if (AttributeParameter.class.isInstance(queryParameter)) {
                        String attributeParameterName = ((AttributeParameter) queryParameter).getName();

                        Property<Serializable> property = PropertyQueries.<Serializable>createQuery(identityQuery.getIdentityType())
                                .addCriteria(new NamedPropertyCriteria(attributeParameterName))
                                .getSingleResult();

                        if (property.getName().equals(attributeParameterName)) {
                            Serializable storedValue = property.getValue(storedEntry);

                            if (storedValue != null) {
                                Object[] queryParameterValues = entry.getValue();

                                if (storedValue.getClass().isArray() || Collection.class.isInstance(storedValue)) {
                                    // TODO: handle multi-valued properties
                                } else {
                                    found = storedValue != null && storedValue.equals(queryParameterValues[0]);
                                }
                            }
                        }
                    }
                }

                if (found) {
                    result.add((V) cloneAttributedType(context, storedEntry));
                }
            }
        }

        // Apply pagination
        if (identityQuery.getLimit() > 0) {
            int numberOfItems = Math.min(identityQuery.getLimit(), result.size() - identityQuery.getOffset());
            result = result.subList(identityQuery.getOffset(), identityQuery.getOffset() + numberOfItems);
        }

        return result;
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

    <T extends Relationship> T convertToRelationship(IdentityContext context, FileRelationship fileRelationship) {
        Class<T> relationshipType = null;

        try {
            relationshipType = (Class<T>) Class.forName(fileRelationship.getType());
        } catch (Exception e) {
            throw MESSAGES.classNotFound(fileRelationship.getType());
        }

        return cloneRelationship(context, fileRelationship, relationshipType);
    }

    private <T extends Relationship> T cloneRelationship(IdentityContext context, FileRelationship fileRelationship,
                                                         Class<? extends Relationship> relationshipType) {
        T clonedRelationship = null;

        try {
            clonedRelationship = (T) relationshipType.newInstance();
        } catch (Exception e) {
            throw MESSAGES.instantiationError(relationshipType.getName(), e);
        }

        clonedRelationship.setId(fileRelationship.getEntry().getId());

        List<Property<IdentityType>> relationshipIdentityTypes = PropertyQueries
                .<IdentityType>createQuery(clonedRelationship.getClass())
                .addCriteria(new TypedPropertyCriteria(IdentityType.class)).getResultList();

        for (Property<IdentityType> annotatedProperty : relationshipIdentityTypes) {
            try {
                IdentityType identityType = annotatedProperty.getJavaClass().newInstance();

                identityType.setId(fileRelationship.getIdentityTypeId(annotatedProperty.getName()));

                annotatedProperty.setValue(clonedRelationship, identityType);
            } catch (Exception e) {
                throw new IdentityManagementException("Could not create relationship.", e);
            }
        }

        updateAttributedType(fileRelationship.getEntry(), clonedRelationship);

        return clonedRelationship;
    }

    boolean hasParentGroup(Group childGroup, Group parentGroup) {
        if (childGroup.getParentGroup() != null && parentGroup != null) {
            if (childGroup.getParentGroup().getId().equals(parentGroup.getId())) {
                return true;
            }
        } else {
            return false;
        }

        return hasParentGroup(childGroup.getParentGroup(), parentGroup);
    }

    Map<String, List<FileRelationship>> getRelationships() {
        return this.fileDataSource.getRelationships();
    }

    /**
     * <p>Resolves the corresponding {@link FilePartition} for the given {@link Partition}.</p>
     *
     * @param partition
     * @return
     * @throws IdentityManagementException if no {@link FilePartition} exists for the given partition
     */
    private FilePartition resolve(Class<? extends Partition> type, String name) throws IdentityManagementException {
        for (FilePartition filePartition : this.fileDataSource.getPartitions().values()) {
            Partition storedPartition = filePartition.getEntry();

            if (storedPartition.getClass().equals(type) && storedPartition.getName().equals(name)) {
                return filePartition;
            }
        }

        throw MESSAGES.partitionNotFoundWithName(type, name);
    }

    private void updateAttributedType(AttributedType fromIdentityType, AttributedType toIdentityType) {
        toIdentityType.setId(fromIdentityType.getId());

        for (Object object : toIdentityType.getAttributes().toArray()) {
            Attribute<? extends Serializable> attribute = (Attribute<? extends Serializable>) object;
            toIdentityType.removeAttribute(attribute.getName());
        }

        List<Property<Serializable>> attributeProperties = PropertyQueries
                .<Serializable>createQuery(fromIdentityType.getClass())
                .addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class)).getResultList();

        for (Property<Serializable> attributeProperty : attributeProperties) {
            attributeProperty.setValue(toIdentityType, attributeProperty.getValue(fromIdentityType));
        }

        for (Attribute<? extends Serializable> attrib : fromIdentityType.getAttributes()) {
            toIdentityType.setAttribute(attrib);
        }
    }

    private <T extends AttributedType> T cloneAttributedType(IdentityContext context, T attributedType) {
        T clonedAttributedType = null;

        if (Partition.class.isInstance(attributedType)) {
            Partition partition = (Partition) attributedType;

            try {
                clonedAttributedType = (T) attributedType.getClass().getConstructor(new Class[]{String.class}).newInstance(partition.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                clonedAttributedType = (T) attributedType.getClass().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        clonedAttributedType.setId(attributedType.getId());

        PropertyQuery<Serializable> query = PropertyQueries.createQuery(attributedType.getClass());

        query.addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class));

        for (Property<Serializable> property : query.getResultList()) {
            property.setValue(clonedAttributedType, property.getValue(attributedType));
        }

        for (Attribute<? extends Serializable> attribute : attributedType.getAttributes()) {
            clonedAttributedType.setAttribute(attribute);
        }

        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;

            identityType.setPartition(context.getPartition());

            IdentityType clonedIdentityType = (IdentityType) clonedAttributedType;

            clonedIdentityType.setPartition(identityType.getPartition());
            clonedIdentityType.setExpirationDate(identityType.getExpirationDate());
            clonedIdentityType.setCreatedDate(identityType.getCreatedDate());
            clonedIdentityType.setEnabled(identityType.isEnabled());
        }

        return clonedAttributedType;
    }
}