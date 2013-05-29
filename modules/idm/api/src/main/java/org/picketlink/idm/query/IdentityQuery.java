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

import org.picketlink.idm.model.IdentityType;

import java.util.List;
import java.util.Map;

/**
 * Unified identity query API
 *
 * @author Shane Bryzak
 */
public interface IdentityQuery<T extends IdentityType> {

    IdentityQuery<T> setOffset(int offset);

    IdentityQuery<T> setLimit(int limit);

    /**
     * Parameters used to sort the results. First parameter has biggest priority.
     * For example: setSortParameter(User.LAST_NAME, User.FIRST_NAME) means that results will be sorted primarily by lastName
     * and firstName will be used to sort only records with same lastName
     *
     * @param sortParameters parameters to specify sort criteria
     * @return this query
     */
    IdentityQuery<T> setSortParameters(QueryParameter... sortParameters);

    /**
     * @see #setSortParameters(QueryParameter...)
     */
    QueryParameter[] getSortParameters();

    /**
     * Specify if sorting will be ascending (true) or descending (false)
     * @param sortAscending to specify if sorting will be ascending or descending
     * @return this query
     */
    IdentityQuery<T> setSortAscending(boolean sortAscending);

    /**
     * @return true if sorting will be ascending
     * @see #setSortAscending(boolean)
     */
    boolean isSortAscending();

    IdentityQuery<T> setParameter(QueryParameter param, Object... value);

    Class<T> getIdentityType();

    Map<QueryParameter, Object[]> getParameters();

    Object[] getParameter(QueryParameter queryParameter);

    Map<QueryParameter, Object[]> getParameters(Class<?> type);

    int getOffset();

    int getLimit();

    List<T> getResultList();

   /**
    * Count of all query results. It takes into account query parameters, but it doesn't take into account pagination
    * parameter like offset and limit
    *
    * @return count of all query results
    */
    int getResultCount();
}
