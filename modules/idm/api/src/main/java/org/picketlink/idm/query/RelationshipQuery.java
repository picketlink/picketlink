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

package org.picketlink.idm.query;

import org.picketlink.idm.model.Relationship;

import java.util.List;
import java.util.Map;

/**
 * Used to query identity relationships
 *
 * @author Shane Bryzak
 */
public interface RelationshipQuery<T extends Relationship> {
    RelationshipQuery<T> setOffset(int offset);

    RelationshipQuery<T> setLimit(int limit);

    RelationshipQuery<T> setParameter(QueryParameter param, Object... value);

    Map<QueryParameter, Object[]> getParameters();

    Object[] getParameter(QueryParameter queryParameter);

    Class<T> getRelationshipClass();

    int getOffset();

    int getLimit();

    List<T> getResultList();

    int getResultCount();
}
