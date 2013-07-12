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
import java.util.Collections;
import java.util.HashMap;
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
import org.picketlink.idm.credential.internal.CredentialUtils;
import org.picketlink.idm.credential.internal.DigestCredentialHandler;
import org.picketlink.idm.credential.internal.PasswordCredentialHandler;
import org.picketlink.idm.credential.internal.TOTPCredentialHandler;
import org.picketlink.idm.credential.internal.X509CertificateCredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.CredentialHandlers;
import org.picketlink.idm.internal.AbstractIdentityStore;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.sample.Group;
import org.picketlink.idm.model.sample.GroupMembership;
import org.picketlink.idm.model.sample.GroupRole;
import org.picketlink.idm.model.sample.Realm;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.query.RelationshipQueryParameter;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.PartitionStore;
import static java.util.Map.Entry;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.file.internal.FileIdentityQueryHelper.isQueryParameterEquals;

/**
 * <p>
 * File based {@link org.picketlink.idm.spi.IdentityStore} implementation.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@CredentialHandlers({PasswordCredentialHandler.class, X509CertificateCredentialHandler.class, DigestCredentialHandler.class, TOTPCredentialHandler.class})
public class FileIdentityStore extends AbstractIdentityStore<FileIdentityStoreConfiguration>
        implements PartitionStore<FileIdentityStoreConfiguration>, CredentialStore<FileIdentityStoreConfiguration> {

    private FileDataSource fileDataSource;

    @Override
    public void setup(FileIdentityStoreConfiguration configuration) {
        super.setup(configuration);

        this.fileDataSource = new FileDataSource(configuration);
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
        attributedType.setId(context.getIdGenerator().generate());

        AttributedType newAttributedType = cloneAttributedType(context, attributedType);

        if (IdentityType.class.isInstance(newAttributedType)) {
            FilePartition filePartition = resolve(context.getPartition().getClass(), context.getPartition().getName());

            filePartition.getAttributedTypes().put(newAttributedType.getId(), new FileIdentityType((IdentityType) newAttributedType));

            this.fileDataSource.flushAttributedTypes(filePartition);
        } else if (Relationship.class.isInstance(newAttributedType)) {
            Relationship relationship = (Relationship) newAttributedType;
            String type = relationship.getClass().getName();

            List<FileRelationship> storedRelationships = this.fileDataSource.getRelationships().get(type);

            if (storedRelationships == null) {
                storedRelationships = new ArrayList<FileRelationship>();
                this.fileDataSource.getRelationships().put(type, storedRelationships);
            }

            storedRelationships.add(new FileRelationship((Relationship) newAttributedType));

            this.fileDataSource.flushRelationships();
        } else {
            throw MESSAGES.attributedTypeUnsupportedType(attributedType.getClass());
        }
    }

    @Override
    public void update(IdentityContext context, final AttributedType attributedType) {
        AttributedType updatedAttributedType = cloneAttributedType(context, (attributedType));

        if (IdentityType.class.isInstance(attributedType)) {
            FilePartition filePartition = resolve(context.getPartition().getClass(), context.getPartition().getName());

            filePartition.getAttributedTypes().put(attributedType.getId(), new FileIdentityType((IdentityType) updatedAttributedType));

            this.fileDataSource.flushAttributedTypes(filePartition);
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) updatedAttributedType;
            String type = relationship.getClass().getName();

            List<FileRelationship> storedRelationships = this.fileDataSource.getRelationships().get(type);

            for (FileRelationship storedRelationship : new ArrayList<FileRelationship>(storedRelationships)) {
                if (storedRelationship.getId().equals(updatedAttributedType.getId())) {
                    storedRelationships.remove(storedRelationship);
                }
            }

            storedRelationships.add(new FileRelationship((Relationship) updatedAttributedType));

            this.fileDataSource.flushRelationships();
        }
    }

    @Override
    public void remove(IdentityContext context, AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            resolve(context.getPartition().getClass(), context.getPartition().getName()).getAttributedTypes().remove(attributedType.getId());
            removeRelationships((IdentityType) attributedType);
        } else if (Relationship.class.isInstance(attributedType)) {
            List<FileRelationship> fileRelationships = this.fileDataSource.getRelationships().get(attributedType.getClass().getName());

            for (FileRelationship fileRelationship : new ArrayList<FileRelationship>(fileRelationships)) {
                if (fileRelationship.getId().equals(attributedType.getId())) {
                    fileRelationships.remove(fileRelationship);
                }
            }
        }

        this.fileDataSource.flushRelationships();
    }

    @Override
    public void storeCredential(IdentityContext context, Account account, CredentialStorage storage) {
        List<FileCredentialStorage> credentials = getCredentials(context, account, storage.getClass());

        credentials.add(new FileCredentialStorage(storage));

        this.fileDataSource.flushCredentials((Realm) context.getPartition());
    }

    @Override
    public <T extends CredentialStorage> T retrieveCurrentCredential(IdentityContext context, Account account, Class<T> storageClass) {
        return CredentialUtils.getCurrentCredential(context, account, this, storageClass);
    }

    @Override
    public <T extends CredentialStorage> List<T> retrieveCredentials(IdentityContext context, Account account, Class<T> storageClass) {
        ArrayList<T> storedCredentials = new ArrayList<T>();

        List<FileCredentialStorage> credentials = getCredentials(context, account, storageClass);

        for (FileCredentialStorage fileCredentialStorage : credentials) {
            storedCredentials.add((T) fileCredentialStorage.getEntry());
        }

        return storedCredentials;
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
            if (ids[0] != null) {
                AbstractFileAttributedType fileAttributedType = filePartition.getAttributedTypes().get(ids[0]);

                if (fileAttributedType != null) {
                    result.add(cloneAttributedType(context, (V) fileAttributedType.getEntry()));
                }
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

                if (!isQueryParameterEquals(identityQuery, IdentityType.ENABLED, storedEntry.isEnabled())) {
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

                    if (queryParameter.equals(IdentityType.CREATED_DATE)
                            || queryParameter.equals(IdentityType.CREATED_BEFORE)
                            || queryParameter.equals(IdentityType.CREATED_AFTER)) {
                        found = queryHelper.matchCreatedDateParameters(storedEntry);
                        continue;
                    }

                    if (queryParameter.equals(IdentityType.EXPIRY_DATE)
                            || queryParameter.equals(IdentityType.EXPIRY_BEFORE)
                            || queryParameter.equals(IdentityType.EXPIRY_AFTER)) {
                        found = queryHelper.matchExpiryDateParameters(storedEntry);
                        continue;
                    }

                    if (AttributeParameter.class.isInstance(queryParameter)) {
                        String attributeParameterName = ((AttributeParameter) queryParameter).getName();

                        Property<Serializable> property = null;

                        try {
                            property = PropertyQueries.<Serializable>createQuery(identityQuery.getIdentityType())
                                    .addCriteria(new NamedPropertyCriteria(attributeParameterName))
                                    .getSingleResult();
                        } catch (RuntimeException re) {
                        }

                        Object[] queryParameterValues = entry.getValue();

                        if (property != null && property.getName().equals(attributeParameterName)) {
                            Serializable storedValue = property.getValue(storedEntry);

                            if (storedValue != null) {
                                if (storedValue.getClass().isArray() || Collection.class.isInstance(storedValue)) {
                                    // TODO: handle multi-valued properties
                                } else {
                                    found = storedValue != null && storedValue.equals(queryParameterValues[0]);
                                }
                            }
                        } else {
                            Attribute<Serializable> identityTypeAttribute = storedEntry.getAttribute(attributeParameterName);

                            if (identityTypeAttribute != null && identityTypeAttribute.getValue() != null) {
                                int valuesMatchCount = queryParameterValues.length;

                                for (Object value : queryParameterValues) {
                                    if (identityTypeAttribute.getValue().getClass().isArray()) {
                                        Object[] userValues = (Object[]) identityTypeAttribute.getValue();

                                        for (Object object : userValues) {
                                            if (object.equals(value)) {
                                                valuesMatchCount--;
                                            }
                                        }
                                    } else {
                                        if (value.equals(identityTypeAttribute.getValue())) {
                                            valuesMatchCount--;
                                        }
                                    }
                                }

                                found = valuesMatchCount <= 0;
                            }
                        }
                    }
                }

                if (found) {
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
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        int limit = identityQuery.getLimit();
        int offset = identityQuery.getOffset();

        identityQuery.setLimit(0);
        identityQuery.setOffset(0);

        int resultCount = identityQuery.getResultList().size();

        identityQuery.setLimit(limit);
        identityQuery.setOffset(offset);

        return resultCount;
    }

    @Override
    public <V extends Relationship> List<V> fetchQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return fetchQueryResults(context, query, false);
    }

    @Override
    public <V extends Relationship> int countQueryResults(IdentityContext context, RelationshipQuery<V> query) {
        return fetchQueryResults(context, query).size();
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

    <T extends Relationship> T convertToRelationship(IdentityContext context, FileRelationship fileRelationship) {
        return (T) cloneAttributedType(context, fileRelationship.getEntry());
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
        } else if (Relationship.class.isInstance(attributedType)) {
            Relationship relationship = (Relationship) attributedType;
            Relationship clonedRelationship = (Relationship) clonedAttributedType;

            PropertyQuery<Serializable> identityPropertiesQuery = PropertyQueries.createQuery(relationship.getClass());

            identityPropertiesQuery.addCriteria(new TypedPropertyCriteria(IdentityType.class, true));

            for (Property<Serializable> property : identityPropertiesQuery.getResultList()) {
                property.setValue(clonedRelationship, property.getValue(relationship));
            }
        }

        return clonedAttributedType;
    }

    @SuppressWarnings("unchecked")
    private <T extends Relationship> List<T> fetchQueryResults(IdentityContext context, RelationshipQuery<T> query,
                                                               boolean matchExactGroup) {
        List<T> result = new ArrayList<T>();
        Class<T> relationshipType = query.getRelationshipClass();
        List<FileRelationship> relationships = new ArrayList<FileRelationship>();

        if (Relationship.class.equals(query.getRelationshipClass())) {
            Collection<List<FileRelationship>> allRelationships = this.fileDataSource.getRelationships().values();

            for (List<FileRelationship> partitionRelationships : allRelationships) {
                relationships.addAll(partitionRelationships);
            }
        } else {
            List<FileRelationship> typedRelationship = this.fileDataSource.getRelationships().get(
                    relationshipType.getName());

            if (typedRelationship != null) {
                relationships.addAll(typedRelationship);
            }
        }

        if (relationships.isEmpty()) {
            return result;
        }

        for (FileRelationship storedRelationship : relationships) {
            boolean match = false;

            if (query.getRelationshipClass().isInstance(storedRelationship.getEntry())) {
                for (Entry<QueryParameter, Object[]> entry : query.getParameters().entrySet()) {
                    QueryParameter queryParameter = entry.getKey();
                    Object[] values = entry.getValue();

                    if (Relationship.IDENTITY.equals(queryParameter)) {
                        int valuesMathCount = values.length;

                        for (Object object : values) {
                            IdentityType identityType = (IdentityType) object;

                            if (storedRelationship.hasIdentityType(identityType.getId())) {
                                valuesMathCount--;
                            }
                        }

                        match = valuesMathCount <= 0;
                    }

                    if (queryParameter instanceof RelationshipQueryParameter) {
                        RelationshipQueryParameter identityTypeParameter = (RelationshipQueryParameter) queryParameter;
                        match = matchIdentityType(context, storedRelationship, query, identityTypeParameter,
                                matchExactGroup);
                    }

                    if (AttributeParameter.class.isInstance(queryParameter) && values != null) {
                        AttributeParameter customParameter = (AttributeParameter) queryParameter;
                        Attribute<Serializable> userAttribute = storedRelationship.getEntry().getAttribute(
                                customParameter.getName());

                        Serializable userAttributeValue = null;

                        if (userAttribute != null) {
                            userAttributeValue = userAttribute.getValue();
                        }

                        if (userAttributeValue != null) {
                            int count = values.length;

                            for (Object value : values) {
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

                            match = count <= 0;
                        }
                    }

                    if (!match) {
                        break;
                    }
                }
            }

            if (match) {
                result.add((T) cloneAttributedType(context, storedRelationship.getEntry()));
            }
        }

        return result;
    }

    private boolean matchIdentityType(IdentityContext context, FileRelationship storedRelationship, RelationshipQuery<?> query,
                                      RelationshipQueryParameter identityTypeParameter, boolean matchExactGroup) {
        Object[] values = query.getParameter(identityTypeParameter);
        int valuesMathCount = values.length;

        String identityTypeId = storedRelationship.getIdentityTypeId(identityTypeParameter.getName());

        for (Object object : values) {
            IdentityType identityType = (IdentityType) object;

            if (identityTypeId.equals(identityType.getId())) {
                valuesMathCount--;

                if ((GroupMembership.class.isInstance(storedRelationship.getEntry()) || GroupRole.class
                        .isInstance(storedRelationship.getEntry())) && !matchExactGroup) {
                    if (Group.class.isInstance(identityType)) {
                        valuesMathCount = valuesMathCount - (values.length - 1);
                    }
                }

            }
        }

        return valuesMathCount <= 0;
    }

    private void removeRelationships(IdentityType identityType) {
        for (List<FileRelationship> relationshipsType : this.fileDataSource.getRelationships().values()) {
            for (FileRelationship fileRelationship : new ArrayList<FileRelationship>(relationshipsType)) {
                if (fileRelationship.hasIdentityType(identityType.getId())) {
                    relationshipsType.remove(fileRelationship);
                }
            }
        }

        this.fileDataSource.flushRelationships();
    }

    private List<FileCredentialStorage> getCredentials(IdentityContext context, Account agent,
                                                       Class<? extends CredentialStorage> storageType) {
        Map<String, List<FileCredentialStorage>> agentCredentials = this.fileDataSource.getPartitions().get(context.getPartition().getId()).getCredentials().get(
                agent.getId());

        if (agentCredentials == null) {
            agentCredentials = new HashMap<String, List<FileCredentialStorage>>();
            this.fileDataSource.getPartitions().get(context.getPartition().getId()).getCredentials().put(agent.getId(), agentCredentials);
        }

        List<FileCredentialStorage> credentials = agentCredentials.get(storageType.getName());

        if (credentials == null) {
            credentials = new ArrayList<FileCredentialStorage>();
        }

        agentCredentials.put(storageType.getName(), credentials);

        return credentials;
    }
}