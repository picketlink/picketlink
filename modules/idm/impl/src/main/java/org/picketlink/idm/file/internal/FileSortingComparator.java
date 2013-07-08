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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * Comparator for sorting identity objects according to given query parameters
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FileSortingComparator<T extends IdentityType> implements Comparator<T> {

    private IdentityQuery<T> identityQuery;

    public FileSortingComparator(IdentityQuery<T> identityQuery) {
        this.identityQuery = identityQuery;
    }

    @Override
    public int compare(T o1, T o2) {
        QueryParameter[] params = identityQuery.getSortParameters();

        int sortResult = 0;

        if (params != null && params.length != 0) {
            for (QueryParameter queryParameter : params) {
                sortResult = sortByQueryParameter(queryParameter, o1, o2);
                if (sortResult != 0) {
                    // Negate result if descending order is required
                    if (!identityQuery.isSortAscending()) {
                        return -sortResult;
                    }
                    return sortResult;
                }
            }
        }

        return sortResult;
    }

    protected int sortByQueryParameter(QueryParameter queryParameter, T o1, T o2) {
        if (AttributeParameter.class.isInstance(queryParameter)) {
            AttributeParameter attributeParameter = (AttributeParameter) queryParameter;
            List<Property<Serializable>> attributeProperties = PropertyQueries
                    .<Serializable>createQuery(o1.getClass())
                    .addCriteria(new NamedPropertyCriteria(attributeParameter.getName())).getResultList();

            if (!attributeProperties.isEmpty()) {
                Property<Serializable> property = attributeProperties.get(0);

                Serializable value1 = property.getValue(o1);
                Serializable value2 = property.getValue(o2);

                if (String.class.equals(value1.getClass())) {
                    return value1.toString().compareTo(value2.toString());
                } else if (Date.class.isInstance(value1)) {
                    return ((Date) value1).compareTo((Date) value2);
                } else if (Boolean.class.isInstance(value1)) {
                    return Boolean.valueOf(value1.toString()).compareTo(Boolean.valueOf(value2.toString()));
                }
            }
        } else {
            if (queryParameter.equals(IdentityType.ID)) {
                return o1.getId().compareTo(o2.getId());
            } else if (queryParameter.equals(IdentityType.ENABLED)) {
                return Boolean.valueOf(o1.isEnabled()).compareTo(o2.isEnabled());
            } else if (queryParameter.equals(IdentityType.CREATED_DATE)) {
                return o1.getCreatedDate().compareTo(o2.getCreatedDate());
            } else if (queryParameter.equals(IdentityType.EXPIRY_DATE)) {
                return o1.getExpirationDate().compareTo(o2.getExpirationDate());
            }
        }

        return -1;
    }
}