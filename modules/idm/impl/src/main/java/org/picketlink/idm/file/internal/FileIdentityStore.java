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

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.properties.query.TypedPropertyCriteria;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.credential.handler.DigestCredentialHandler;
import org.picketlink.idm.credential.handler.PasswordCredentialHandler;
import org.picketlink.idm.credential.handler.TOTPCredentialHandler;
import org.picketlink.idm.credential.handler.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.CredentialHandlers;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.internal.RelationshipReference;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Map.Entry;
import static org.picketlink.common.properties.query.TypedPropertyCriteria.MatchOption;
import static org.picketlink.common.reflection.Reflections.newInstance;
import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMInternalMessages.MESSAGES;
import static org.picketlink.idm.credential.util.CredentialUtils.getCurrentCredential;

/**
 * <p> File based {@link IdentityStore} implementation. </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({PasswordCredentialHandler.class, X509CertificateCredentialHandler.class, DigestCredentialHandler.class, TOTPCredentialHandler.class})
public class FileIdentityStore extends AbstractIdentityStore<FileIdentityStoreConfiguration>
        implements PartitionStore<FileIdentityStoreConfiguration>,
        CredentialStore<FileIdentityStoreConfiguration>,
        AttributeStore<FileIdentityStoreConfiguration> {

    private FileDataSource fileDataSource;

    @Override
    public void setup(FileIdentityStoreConfiguration configuration) {
        super.setup(configuration);

        this.fileDataSource = new FileDataSource(configuration);
    }

    @Override
    public void addAttributedType(IdentityContext context, final AttributedType attributedType) {
        AttributedType clonedAttributedType = cloneAttributedType(context, attributedType);

        if (IdentityType.class.isInstance(clonedAttributedType)) {
            storeIdentityType(context, (IdentityType) clonedAttributedType);
        } else if (Relationship.class.isInstance(clonedAttributedType)) {
            storeRelationshipType((Relationship) clonedAttributedType);
        } else {
            this.fileDataSource.getAttributedTypes().put(attributedType.getId(), new FileAttributedType(attributedType));
        }
    }

    @Override
    public void updateAttributedType(IdentityContext context, final AttributedType attributedType) {
        AttributedType updatedAttributedType = cloneAttributedType(context, (attributedType));

        if (IdentityType.class.isInstance(attributedType)) {
            storeIdentityType(context, (IdentityType) updatedAttributedType);
        } else if (Relationship.class.isInstance(attributedType)) {
            storeRelationshipType((Relationship) updatedAttributedType);
        }
    }

    @Override
    public void removeAttributedType(IdentityContext context, AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;

            Partition partition = identityType.getPartition();
            FilePartition filePartition = resolve(partition.getClass(), partition.getName());

            Map<String, FileIdentityType> identityTypes = filePartition.getIdentityTypes().get(attributedType.getClass().getName());

            if (identityTypes != null) {
                identityTypes.remove(identityType.getId());
            }

            this.fileDataSource.flushAttributedTypes(filePartition);
        } else if (Relationship.class.isInstance(attributedType)) {
            Map<String, FileRelationship> fileRelationships = this.fileDataSource.getRelationships().get(attributedType.getClass().getName());

            for (FileRelationship fileRelationship : new HashMap<String, FileRelationship>(fileRelationships).values()) {
                if (fileRelationship.getId().equals(attributedType.getId())) {
                    fileRelationships.remove(fileRelationship.getId());
                }
            }

            this.fileDataSource.flushRelationships();
        } else {
            this.fileDataSource.getAttributedTypes().remove(attributedType.getId());
            this.fileDataSource.flushAttributedTypes();
        }
    }

    @Override
    protected void removeFromRelationships(IdentityContext context, IdentityType identityType) {
        Map<String, Map<String, FileRelationship>> relationships = this.fileDataSource.getRelationships();
        for (Map<String, FileRelationship> relationshipsType : relationships.values()) {
            for (FileRelationship fileRelationship : new HashMap<String, FileRelationship>(relationshipsType).values()) {
                if (fileRelationship.hasIdentityType(identityType)) {
                    relationshipsType.remove(fileRelationship.getId());
                }
            }
        }

        this.fileDataSource.flushRelationships();
    }

    @Override
    protected void removeCredentials(IdentityContext context, Account account) {
        Partition partition = account.getPartition();
        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        Map<String, Map<String, List<FileCredentialStorage>>> credentials = filePartition.getCredentials();

        credentials.remove(account.getId());

        this.fileDataSource.flushCredentials(filePartition);
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
            return (P) cloneAttributedType(identityContext, (P) resolve(partitionClass, name).getEntry());
        } catch (IdentityManagementException ime) {
            //just ignore if not found.
        }

        return null;
    }

    @Override
    public <P extends Partition> List<P> get(IdentityContext identityContext, Class<P> partitionClass) {
        List<P> result = new ArrayList<P>();

        for (FilePartition filePartition : this.fileDataSource.getPartitions().values()) {
            Partition partition = filePartition.getEntry();

            if (Partition.class.equals(partitionClass) || partitionClass.equals(partition.getClass())) {
                result.add((P) cloneAttributedType(identityContext, partition));
            }
        }

        return result;
    }

    @Override
    public <P extends Partition> P lookupById(final IdentityContext context, final Class<P> partitionClass,
                                              final String id) {
        FilePartition filePartition = this.fileDataSource.getPartitions().get(id);

        if (filePartition != null && partitionClass.isInstance(filePartition.getEntry())) {
            return (P) cloneAttributedType(context, filePartition.getEntry());
        }

        return null;
    }

    @Override
    public String getConfigurationName(IdentityContext identityContext, Partition partition) {
        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        if (isNullOrEmpty(filePartition.getConfigurationName())) {
            throw MESSAGES.partitionWithNoConfigurationName(partition);
        }

        return filePartition.getConfigurationName();
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        List<FileCredentialStorage> credentials = getCredentials(account, storage.getClass());

        credentials.add(new FileCredentialStorage(storage));

        Partition partition = context.getPartition();

        this.fileDataSource.flushCredentials(resolve(partition.getClass(), partition.getName()));
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return getCurrentCredential(context, account, this, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass) {
        List<T> storedCredentials = new ArrayList<T>();

        List<FileCredentialStorage> credentials = getCredentials(account, storageClass);

        for (FileCredentialStorage fileCredentialStorage : credentials) {
            storedCredentials.add((T) fileCredentialStorage.getEntry());
        }

        Collections.sort(storedCredentials, new Comparator<T>() {
            @Override
            public int compare(final T o1, final T o2) {
                return o2.getEffectiveDate().compareTo(o1.getEffectiveDate());
            }
        });

        return storedCredentials;
    }

    @Override
    public <V extends IdentityType> List<V> fetchQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        Object[] partitionParameters = identityQuery.getParameter(IdentityType.PARTITION);
        Partition partition = null;

        if (partitionParameters == null) {
            partition = context.getPartition();
        } else {
            partition = (Partition) partitionParameters[0];
        }

        FilePartition filePartition = resolve(partition.getClass(), partition.getName());

        List<V> result = new ArrayList<V>();

        Object[] ids = identityQuery.getParameter(IdentityType.ID);

        Map<String, Map<String, FileIdentityType>> identityTypes = filePartition.getIdentityTypes();
        Map<String, FileIdentityType> typedIdentityTypes = null;

        if (IdentityType.class.equals(identityQuery.getIdentityType())) {
            typedIdentityTypes = new HashMap<String, FileIdentityType>();
            for (String type : identityTypes.keySet()) {
                typedIdentityTypes.putAll(identityTypes.get(type));
            }
        } else {
            typedIdentityTypes = identityTypes.get(identityQuery.getIdentityType().getName());
        }

        if (typedIdentityTypes == null) {
            return result;
        }

        if (ids != null && ids.length > 0) {
            if (ids[0] != null) {
                FileIdentityType fileAttributedType = typedIdentityTypes.get(ids[0]);

                if (fileAttributedType != null) {
                    result.add(cloneAttributedType(context, (V) fileAttributedType.getEntry()));
                }
            }
        } else {
            for (FileIdentityType storedIdentityType : typedIdentityTypes.values()) {
                IdentityType storedEntry = (IdentityType) storedIdentityType.getEntry();

                boolean match = identityQuery.getParameters().isEmpty();

                for (Entry<QueryParameter, Object[]> entry : identityQuery.getParameters().entrySet()) {
                    QueryParameter queryParameter = entry.getKey();

                    if (AttributeParameter.class.isInstance(queryParameter)) {
                        AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
                        String attributeParameterName = attributeParameter.getName();

                        Property<Serializable> property = PropertyQueries.<Serializable>createQuery(identityQuery.getIdentityType())
                                .addCriteria(new NamedPropertyCriteria(attributeParameterName))
                                .getFirstResult();

                        Object[] parameterValues = entry.getValue();

                        if (property != null && property.getName().equals(attributeParameterName)) {
                            Serializable storedValue = property.getValue(storedEntry);

                            if (storedValue != null) {
                                if (storedValue.getClass().isArray() || Collection.class.isInstance(storedValue)) {
                                    // TODO: handle multi-valued properties
                                } else {
                                    if (queryParameter.equals(IdentityType.CREATED_BEFORE) || queryParameter.equals(IdentityType.EXPIRY_BEFORE)) {
                                        match = storedValue != null && ((Date) storedValue).compareTo((Date) parameterValues[0]) <= 0;
                                    } else if (queryParameter.equals(IdentityType.CREATED_AFTER) || queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
                                        match = storedValue != null && ((Date) storedValue).compareTo((Date) parameterValues[0]) >= 0;
                                    } else {
                                        match = storedValue != null && storedValue.equals(parameterValues[0]);
                                    }
                                }
                            }
                        } else {
                            loadAttributes(context, storedEntry);
                            match = matchAttribute(storedEntry, attributeParameterName, parameterValues);
                        }

                        if (!match) {
                            break;
                        }
                    }
                }

                if (match) {
                    result.add((V) cloneAttributedType(context, storedEntry));
                }
            }
        }

        // Apply sorting
        Collections.sort(result, new FileSortingComparator<V>(identityQuery));

        // Apply pagination
        if (identityQuery.getLimit() > 0) {
            int numberOfItems = Math.min(identityQuery.getLimit(), result.size() - identityQuery.getOffset());
            result = result.subList(identityQuery.getOffset(), identityQuery.getOffset() + numberOfItems);
        }

        return result;
    }

    @Override
    public <T extends Relationship> List<T> fetchQueryResults(IdentityContext context, RelationshipQuery<T> query) {
        List<T> result = new ArrayList<T>();
        Class<T> typeToSearch = query.getRelationshipClass();
        Object[] idParameter = query.getParameter(Relationship.ID);

        if (idParameter != null && idParameter.length > 0) {
            String id = idParameter[0].toString();

            for (Map<String, FileRelationship> partitionRelationships : this.fileDataSource.getRelationships().values()) {
                FileRelationship storedRelationship = partitionRelationships.get(id);

                if (storedRelationship != null && typeToSearch.isAssignableFrom(storedRelationship.getEntry().getClass())) {
                    result.add((T) cloneAttributedType(context, storedRelationship.getEntry()));
                    return result;
                }
            }
        } else {
            List<FileRelationship> relationships = new ArrayList<FileRelationship>();

            if (Relationship.class.equals(typeToSearch)) {
                for (Map<String, FileRelationship> partitionRelationships : this.fileDataSource.getRelationships().values()) {
                    relationships.addAll(partitionRelationships.values());
                }
            } else {
                Map<String, FileRelationship> typedRelationship = this.fileDataSource.getRelationships().get(
                        typeToSearch.getName());

                if (typedRelationship != null) {
                    relationships.addAll(typedRelationship.values());
                }
            }

            for (FileRelationship storedRelationship : relationships) {
                boolean match = query.getParameters().isEmpty();

                if (typeToSearch.isInstance(storedRelationship.getEntry())) {
                    for (Entry<QueryParameter, Object[]> entry : query.getParameters().entrySet()) {
                        QueryParameter queryParameter = entry.getKey();
                        Object[] values = entry.getValue();

                        if (Relationship.IDENTITY.equals(queryParameter)) {
                            int valuesMathCount = values.length;

                            for (Object object : values) {
                                IdentityType identityType = (IdentityType) object;

                                if (storedRelationship.hasIdentityType(identityType)) {
                                    valuesMathCount--;
                                }
                            }

                            match = valuesMathCount <= 0;
                        } else if (queryParameter instanceof RelationshipQueryParameter) {
                            RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) queryParameter;

                            for (Object value : values) {
                                IdentityType identityType = (IdentityType) value;
                                String identityTypeId = storedRelationship.getIdentityTypeId(identityTypeParameter.getName());

                                match = identityTypeId != null && identityTypeId.equals(RelationshipReference
                                        .formatId(identityType));
                            }
                        } else if (AttributeParameter.class.isInstance(queryParameter) && values != null) {
                            AttributeParameter attributeParameter = (AttributeParameter) queryParameter;

                            Property<Serializable> property = PropertyQueries.<Serializable>createQuery(query.getRelationshipClass())
                                    .addCriteria(new NamedPropertyCriteria(attributeParameter.getName()))
                                    .getFirstResult();

                            if (property != null) {
                                Serializable value = property.getValue(storedRelationship.getEntry());

                                if (value != null) {
                                    match = value.equals(values[0]);
                                }
                            } else {
                                loadAttributes(context, storedRelationship.getEntry());
                                match = matchAttribute(storedRelationship.getEntry(), attributeParameter.getName(), values);
                            }
                        }

                        if (!match) {
                            break;
                        }
                    }
                }

                if (match) {
                    T relationship = (T) cloneAttributedType(context, storedRelationship.getEntry());

                    List<Property<IdentityType>> properties = PropertyQueries.<IdentityType>createQuery(query
                            .getRelationshipClass())
                            .addCriteria(new TypedPropertyCriteria(IdentityType.class, MatchOption.SUB_TYPE))
                            .getResultList();

                    RelationshipReference reference = new RelationshipReference(relationship);

                    for (Property<IdentityType> property : properties) {
                        reference.addIdentityTypeReference(property.getName(), storedRelationship.getIdentityTypeId
                                (property.getName()));
                    }

                    result.add((T) reference);
                }
            }
        }

        return result;
    }

    @Override
    public void setAttribute(IdentityContext context, AttributedType type, Attribute<? extends Serializable> attribute) {
        FileAttribute fileAttribute = getFileAttribute(type);

        if (fileAttribute == null) {
            fileAttribute = new FileAttribute(type);
        }

        removeAttribute(context, type, attribute.getName());
        fileAttribute.getEntry().add(attribute);

        this.fileDataSource.getAttributes().put(type.getId(), fileAttribute);
        this.fileDataSource.flushAttributes();
    }

    private FileAttribute getFileAttribute(final AttributedType type) {
        return this.fileDataSource.getAttributes().get(type.getId());
    }

    @Override
    public <V extends Serializable> Attribute<V> getAttribute(IdentityContext context, AttributedType type, String attributeName) {
        FileAttribute fileAttribute = getFileAttribute(type);

        if (fileAttribute != null) {
            for (Attribute<? extends Serializable> attribute : fileAttribute.getEntry()) {
                if (attribute.getName().equals(attributeName)) {
                    return (Attribute<V>) attribute;
                }
            }
        }

        return null;
    }

    @Override
    public void loadAttributes(IdentityContext context, AttributedType attributedType) {
        FileAttribute fileAttribute = getFileAttribute(attributedType);

        if (fileAttribute != null) {
            for (Attribute<? extends Serializable> attribute : fileAttribute.getEntry()) {
                attributedType.setAttribute(attribute);
            }
        }
    }

    @Override
    public void removeAttribute(IdentityContext context, AttributedType type, String attributeName) {
        FileAttribute fileAttribute = getFileAttribute(type);

        if (fileAttribute != null) {
            for (Attribute<? extends Serializable> attribute : new ArrayList<Attribute<? extends Serializable>>
                    (fileAttribute.getEntry())) {
                if (attribute.getName().equals(attributeName)) {
                    fileAttribute.getEntry().remove(attribute);
                }
            }
        }

        this.fileDataSource.flushAttributes();
    }

    /**
     * <p>Resolves the corresponding {@link FilePartition} for the given {@link Partition}.</p>
     *
     * @param partition
     *
     * @return
     *
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

    private <T extends AttributedType> T cloneAttributedType(IdentityContext context, T attributedType) {
        T clonedAttributedType = null;

        try {
            clonedAttributedType = (T) newInstance(attributedType.getClass());
        } catch (Exception e) {
            throw MESSAGES.instantiationError(attributedType.getClass(), e);
        }

        clonedAttributedType.setId(attributedType.getId());

        PropertyQuery<Serializable> query = PropertyQueries.createQuery(attributedType.getClass());

        query.addCriteria(new AnnotatedPropertyCriteria(AttributeProperty.class));

        for (Property<Serializable> property : query.getResultList()) {
            property.setValue(clonedAttributedType, property.getValue(attributedType));
        }

        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;

            identityType.setPartition(context.getPartition());

            IdentityType clonedIdentityType = (IdentityType) clonedAttributedType;

            clonedIdentityType.setPartition(identityType.getPartition());
            clonedIdentityType.setExpirationDate(identityType.getExpirationDate());
            clonedIdentityType.setCreatedDate(identityType.getCreatedDate());
            clonedIdentityType.setEnabled(identityType.isEnabled());
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;
            Relationship clonedRelationship = (Relationship) clonedAttributedType;

            PropertyQuery<Serializable> identityPropertiesQuery = PropertyQueries.createQuery(relationship.getClass());

            identityPropertiesQuery.addCriteria(new TypedPropertyCriteria(IdentityType.class, MatchOption.SUB_TYPE));

            for (Property<Serializable> property : identityPropertiesQuery.getResultList()) {
                property.setValue(clonedRelationship, property.getValue(relationship));
            }
        }

        return clonedAttributedType;
    }

    private List<FileCredentialStorage> getCredentials(Account account, Class<? extends CredentialStorage> storageType) {
        Partition partition = account.getPartition();
        FilePartition filePartition = resolve(partition.getClass(), partition.getName());
        Map<String, List<FileCredentialStorage>> agentCredentials = filePartition.getCredentials().get(account.getId());

        if (agentCredentials == null) {
            agentCredentials = new ConcurrentHashMap<String, List<FileCredentialStorage>>();
            this.fileDataSource.getPartitions().get(partition.getId()).getCredentials().put(account.getId(), agentCredentials);
        }

        List<FileCredentialStorage> credentials = agentCredentials.get(storageType.getName());

        if (credentials == null) {
            credentials = Collections.synchronizedList(new ArrayList<FileCredentialStorage>());
        }

        agentCredentials.put(storageType.getName(), credentials);

        return credentials;
    }

    private void storeRelationshipType(Relationship relationship) {
        String type = relationship.getClass().getName();

        Map<String, FileRelationship> storedRelationships = this.fileDataSource.getRelationships().get(type);

        if (storedRelationships == null) {
            storedRelationships = new ConcurrentHashMap<String, FileRelationship>();
            this.fileDataSource.getRelationships().put(type, storedRelationships);
        }

        storedRelationships.put(relationship.getId(), new FileRelationship(relationship));

        this.fileDataSource.flushRelationships();
    }

    private void storeIdentityType(IdentityContext context, IdentityType identityType) {
        FilePartition filePartition = resolve(context.getPartition().getClass(), context.getPartition().getName());

        Map<String, FileIdentityType> identityTypes = filePartition.getIdentityTypes().get(identityType.getClass().getName());

        if (identityTypes == null) {
            identityTypes = new ConcurrentHashMap<String, FileIdentityType>();
            filePartition.getIdentityTypes().put(identityType.getClass().getName(), identityTypes);
        }

        identityTypes.put(identityType.getId(), new FileIdentityType(identityType));

        this.fileDataSource.flushAttributedTypes(filePartition);
    }

    private boolean matchAttribute(AttributedType attributedType, String parameterName, Object[] valuesToCompare) {
        Attribute<Serializable> userAttribute = attributedType.getAttribute(parameterName);
        Serializable userAttributeValue = null;

        if (userAttribute != null) {
            userAttributeValue = userAttribute.getValue();
        }

        if (userAttributeValue != null) {
            int count = valuesToCompare.length;

            for (Object value : valuesToCompare) {
                if (userAttributeValue.getClass().isArray()) {
                    Object[] userValues = (Object[]) userAttributeValue;

                    for (Object object : userValues) {
                        if (object.equals(value)) {
                            count--;
                        }
                    }
                } else {
                    if (value.equals(userAttributeValue)) {
                        count--;
                    }
                }
            }

            return count <= 0;
        }

        return false;
    }

}