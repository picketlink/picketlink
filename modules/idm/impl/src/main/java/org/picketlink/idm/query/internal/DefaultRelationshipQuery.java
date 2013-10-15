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

package org.picketlink.idm.query.internal;

import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.internal.RelationshipReference;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Default IdentityQuery implementation.
 *
 * @param <T>
 * @author Shane Bryzak
 */
public class DefaultRelationshipQuery<T extends Relationship> implements RelationshipQuery<T> {

    private Map<QueryParameter, Object[]> parameters = new LinkedHashMap<QueryParameter, Object[]>();
    private IdentityContext context;
    private StoreSelector storeSelector;
    private Class<T> relationshipClass;
    private long offset;
    private long limit;

    public DefaultRelationshipQuery(IdentityContext context, Class<T> relationshipClass, StoreSelector storeSelector) {
        this.context = context;
        this.relationshipClass = relationshipClass;
        this.storeSelector = storeSelector;
    }

    @Override
    public RelationshipQuery<T> setParameter(QueryParameter param, Object... value) {
        parameters.put(param, value);
        return this;
    }

    @Override
    public Class<T> getRelationshipClass() {
        return relationshipClass;
    }

    @Override
    public Map<QueryParameter, Object[]> getParameters() {
        return parameters;
    }

    @Override
    public Object[] getParameter(QueryParameter queryParameter) {
        return this.parameters.get(queryParameter);
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public List<T> getResultList() {
        List<T> result = new ArrayList<T>();

        try {
            for (IdentityStore<?> store : getStores()) {
                List<T> references = store.fetchQueryResults(context, this);

                for (T relationship : references) {
                    if (RelationshipReference.class.isInstance(relationship)) {
                        RelationshipReference reference = (RelationshipReference) relationship;
                        resolveIdentityTypes(reference);
                        relationship = (T) reference.getRelationship();
                    }

                    AttributeStore<?> attributeStore = this.storeSelector.getStoreForAttributeOperation(context);

                    if (attributeStore != null) {
                        attributeStore.loadAttributes(context, relationship);
                    }

                    result.add(relationship);
                }
            }
        } catch (Exception e) {
            throw MESSAGES.queryRelationshipFailed(this, e);
        }

        return result;
    }

    private void resolveIdentityTypes(RelationshipReference reference) {
        Relationship relationship = reference.getRelationship();

        for (String descriptor : reference.getDescriptors()) {
            String partitionId = reference.getPartitionId(descriptor);
            String identityTypeId = reference.getIdentityTypeId(descriptor);

            PartitionManager partitionManager = (PartitionManager) this.storeSelector;
            Partition partition = partitionManager.lookupById(Partition.class, partitionId);

            if (partition == null) {
                throw new IdentityManagementException("No partition [" + partitionId + "] found for " +
                        "referenced IdentityType [" + identityTypeId + "].");
            }

            IdentityManager identityManager = partitionManager.createIdentityManager(partition);

            IdentityType identityType = identityManager.lookupIdentityById(IdentityType.class, identityTypeId);

            if (identityType == null) {
                throw new IdentityManagementException("Referenced IdentityType [" + identityTypeId + "] from " +
                        "" +
                        "relationship " +
                        "[" + relationship.getClass() + "] does not exists in any store.");
            }

            Property<Object> property = PropertyQueries
                    .createQuery(relationship.getClass())
                    .addCriteria(new NamedPropertyCriteria(descriptor))
                    .getSingleResult();

            property.setValue(relationship, identityType);
        }
    }

    @Override
    public long getResultCount() {
        long count = 0;

        try {
            for (IdentityStore<?> store : getStores()) {
                count += store.countQueryResults(context, this);
            }
        } catch (Exception e) {
            throw MESSAGES.queryRelationshipFailed(this, e);
        }

        return count;
    }

    @Override
    public RelationshipQuery<T> setOffset(long offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public RelationshipQuery<T> setLimit(long limit) {
        this.limit = limit;
        return this;
    }

    private Set<IdentityStore<?>> getStores() {
        Set<Partition> partitions = new HashSet<Partition>();

        for (Object param : parameters.values()) {
            if (IdentityType.class.isInstance(param)) {
                partitions.add(((IdentityType) param).getPartition());
            }
        }

        return storeSelector.getStoresForRelationshipQuery(context, relationshipClass, partitions);
    }

}
