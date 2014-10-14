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

import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.Condition;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.Sort;
import org.picketlink.idm.spi.AttributeStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.StoreSelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static org.picketlink.idm.IDMInternalMessages.MESSAGES;
import static org.picketlink.idm.util.IDMUtil.configureDefaultPartition;

/**
 * Default IdentityQuery implementation.
 *
 * @param <T>
 *
 * @author Shane Bryzak
 */
public class DefaultIdentityQuery<T extends IdentityType> implements IdentityQuery<T> {

    private final Map<QueryParameter, Object[]> parameters = new LinkedHashMap<QueryParameter, Object[]>();
    private final IdentityContext context;
    private final Class<T> identityType;
    private final StoreSelector storeSelector;
    private final IdentityQueryBuilder queryBuilder;
    private int offset;
    private int limit;
    private Object paginationContext;
    private QueryParameter[] sortParameters;
    private boolean sortAscending = true;
    private final Set<Condition> conditions = new LinkedHashSet<Condition>();
    private final Set<Sort> ordering = new LinkedHashSet<Sort>();

    public DefaultIdentityQuery(IdentityQueryBuilder queryBuilder, IdentityContext context, Class<T> identityType, StoreSelector storeSelector) {
        this.queryBuilder = queryBuilder;
        this.context = context;
        this.storeSelector = storeSelector;
        this.identityType = identityType;

    }

    @Override
    public IdentityQuery<T> setParameter(QueryParameter queryParameter, Object... value) {
        if (value == null || value.length == 0) {
            throw MESSAGES.nullArgument("Query Parameter values null or empty");
        }

        parameters.put(queryParameter, value);

        if (IdentityType.CREATED_AFTER.equals(queryParameter) || IdentityType.EXPIRY_AFTER.equals(queryParameter)) {
            this.conditions.add(queryBuilder.greaterThanOrEqualTo(queryParameter, value[0]));
        } else if (IdentityType.CREATED_BEFORE.equals(queryParameter) || IdentityType.EXPIRY_BEFORE.equals(queryParameter)) {
            this.conditions.add(queryBuilder.lessThanOrEqualTo(queryParameter, value[0]));
        } else {
            this.conditions.add(queryBuilder.equal(queryParameter, value[0]));
        }

        return this;
    }

    @Override
    public IdentityQuery<T> where(Condition... condition) {
        this.conditions.addAll(Arrays.asList(condition));
        return this;
    }

    @Override
    public IdentityQuery<T> sortBy(Sort... sorts) {
        this.ordering.addAll(Arrays.asList(sorts));
        return this;
    }

    @Override
    public Set<Sort> getSorting() {
        return unmodifiableSet(this.ordering);
    }

    @Override
    public Class<T> getIdentityType() {
        return identityType;
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
    public Map<QueryParameter, Object[]> getParameters(Class<?> type) {
        Map<QueryParameter, Object[]> typedParameters = new HashMap<QueryParameter, Object[]>();

        Set<Entry<QueryParameter, Object[]>> entrySet = this.parameters.entrySet();

        for (Entry<QueryParameter, Object[]> entry : entrySet) {
            if (type.isInstance(entry.getKey())) {
                typedParameters.put(entry.getKey(), entry.getValue());
            }
        }

        return typedParameters;
    }

    @Override
    public int getLimit() {
        return limit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public Object getPaginationContext() {
        return paginationContext;
    }

    @Override
    public QueryParameter[] getSortParameters() {
        return sortParameters;
    }

    @Override
    public boolean isSortAscending() {
        return sortAscending;
    }

    @Override
    public List<T> getResultList() {

        // remove this statement once deprecated methods on IdentityQuery are removed
        if (this.sortParameters != null) {
            for (QueryParameter parameter : this.sortParameters) {
                if (isSortAscending()) {
                    sortBy(this.queryBuilder.asc(parameter));
                } else {
                    sortBy(this.queryBuilder.desc(parameter));
                }
            }
        }

        List<T> result = new ArrayList<T>();

        try {
            Set<IdentityStore<?>> identityStores = this.storeSelector.getStoresForIdentityQuery(this.context, this.getIdentityType());
            AttributeStore<?> attributeStore = this.storeSelector.getStoreForAttributeOperation(context);

            for (IdentityStore<?> store : identityStores) {
                for (T identityType : store.fetchQueryResults(this.context, this)) {
                    configureDefaultPartition(identityType, store, getPartitionManager());

                    if (attributeStore != null) {
                        attributeStore.loadAttributes(this.context, identityType);
                    }

                    result.add(identityType);
                }
            }
        } catch (Exception e) {
            throw MESSAGES.queryIdentityTypeFailed(this, e);
        }

        return result;
    }

    @Override
    public int getResultCount() {
        int count = 0;

        Set<IdentityStore<?>> identityStores = this.storeSelector.getStoresForIdentityQuery(this.context, this.getIdentityType());

        for (IdentityStore<?> store : identityStores) {
            count = count + store.countQueryResults(this.context, this);
        }

        return count;
    }

    @Override
    public IdentityQuery<T> setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public IdentityQuery<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public IdentityQuery<T> setSortParameters(QueryParameter... sortParameters) {
        this.sortParameters = sortParameters;
        return this;
    }

    @Override
    public IdentityQuery<T> setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
        return this;
    }

    @Override
    public IdentityQuery<T> setPaginationContext(Object object) {
        this.paginationContext = object;
        return this;
    }

    @Override
    public Set<Condition> getConditions() {
        return unmodifiableSet(this.conditions);
    }

    private PartitionManager getPartitionManager() {
        return (PartitionManager) this.storeSelector;
    }

}
