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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.QueryParameter;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityStore;

/**
 * Default IdentityQuery implementation.
 * 
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class DefaultRelationshipQuery<T extends Relationship> implements RelationshipQuery<T> {

    private Map<QueryParameter, Object[]> parameters = new LinkedHashMap<QueryParameter, Object[]>();
    private IdentityStore<?> identityStore;
    private Class<T> relationshipType;
    private int offset;
    private int limit;
    
    public DefaultRelationshipQuery(Class<T> relationshipType, IdentityStore<?> identityStore) {
        this.identityStore = identityStore;
        this.relationshipType = relationshipType;
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
    public int getLimit() {
        return limit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public List<T> getResultList() {
        return this.identityStore.fetchQueryResults(this);

    }

    @Override
    public int getResultCount() {
        return this.identityStore.countQueryResults(this);
    }

    @Override
    public RelationshipQuery<T> setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public RelationshipQuery<T> setLimit(int limit) {
        this.limit = limit;
        return this;
    }
}
