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

import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
//import org.picketlink.idm.spi.IdentityStore;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.StoreSelector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
//import java.util.Set;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Default IdentityQuery implementation.
 *
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class DefaultRelationshipQuery<T extends Relationship> implements RelationshipQuery<T> {

    private Map<QueryParameter, Object[]> parameters = new LinkedHashMap<QueryParameter, Object[]>();
    private IdentityContext context;
    private StoreSelector storeSelector;
    private Class<T> relationshipType;
    private long offset;
    private long limit;

    public DefaultRelationshipQuery(IdentityContext context, Class<T> relationshipType, StoreSelector storeSelector) {
        this.context = context;
        this.relationshipType = relationshipType;
        this.storeSelector = storeSelector;
    }

    @Override
    public RelationshipQuery<T> setParameter(QueryParameter param, Object... value) {
        parameters.put(param, value);
        return this;
    }

    @Override
    public Class<T> getRelationshipType() {
        return relationshipType;
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

        //storeSelector.getStoreForRelationshipOperation(context, relationshipClass, partitions)
        try {
            //for (IdentityStore<?> store : stores) {
              //  result.addAll(store.fetchQueryResults(context, this));
            //}
        } catch (Exception e) {
            throw MESSAGES.relationshipQueryFailed(this, e);
        }

        return result;
    }

    @Override
    public long getResultCount() {
        long count = 0;
        //for (IdentityStore<?> store : stores) {
          //  count += store.countQueryResults(context, this);
        //}
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
}
