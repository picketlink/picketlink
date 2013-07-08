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
import java.util.Date;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author Pedro Silva
 *
 */
public class FileIdentityQueryHelper {

    private final IdentityQuery<?> identityQuery;
    private final FileIdentityStore identityStore;

    public FileIdentityQueryHelper(IdentityQuery<?> identityQuery, FileIdentityStore identityStore) {
        this.identityQuery = identityQuery;
        this.identityStore = identityStore;
    }

    public boolean matchCreatedDateParameters(IdentityType identityType) {
        if (identityQuery.getParameter(IdentityType.CREATED_DATE) != null
                || identityQuery.getParameter(IdentityType.CREATED_BEFORE) != null
                || identityQuery.getParameter(IdentityType.CREATED_AFTER) != null) {
            Date createdDate = identityType.getCreatedDate();

            if (createdDate != null) {
                if (!isQueryParameterEquals(identityQuery, IdentityType.CREATED_DATE, createdDate)) {
                    return false;
                }

                if (!isQueryParameterLessThan(identityQuery, IdentityType.CREATED_BEFORE, createdDate.getTime())) {
                    return false;
                }

                if (!isQueryParameterGreaterThan(identityQuery, IdentityType.CREATED_AFTER, createdDate.getTime())) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean isQueryParameterEquals(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
                                                 Serializable valueToCompare) {
        Object[] values = identityQuery.getParameter(queryParameter);

        if (values == null) {
            return true;
        }

        Object value = values[0];

        if (Date.class.isInstance(valueToCompare)) {
            Date parameterDate = (Date) value;
            value = parameterDate.getTime();

            Date toCompareDate = (Date) valueToCompare;
            valueToCompare = toCompareDate.getTime();
        }

        if (values.length > 0 && valueToCompare != null && valueToCompare.equals(value)) {
            return true;
        }

        return false;
    }

    public static boolean isQueryParameterEquals(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
                                                 Date valueToCompare) {
        Object[] values = identityQuery.getParameter(queryParameter);

        if (values == null) {
            return true;
        }
        if (values.length > 0 && valueToCompare != null && valueToCompare.equals(values[0])) {
            return true;
        }

        return false;
    }

    public static boolean isQueryParameterGreaterThan(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
                                                      Long valueToCompare) {
        return isQueryParameterGreaterOrLessThan(identityQuery, queryParameter, valueToCompare, true);
    }

    public static boolean isQueryParameterLessThan(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
                                                   Long valueToCompare) {
        return isQueryParameterGreaterOrLessThan(identityQuery, queryParameter, valueToCompare, false);
    }

    public static boolean isQueryParameterGreaterOrLessThan(IdentityQuery<?> identityQuery, QueryParameter queryParameter,
                                                            Long valueToCompare, boolean greaterThan) {
        Object[] values = identityQuery.getParameter(queryParameter);

        if (values == null) {
            return true;
        }

        long value = 0;

        if (Date.class.isInstance(values[0])) {
            Date parameterDate = (Date) values[0];
            value = parameterDate.getTime();
        } else {
            value = Long.valueOf(values[0].toString());
        }

        if (values.length > 0 && valueToCompare != null) {
            if (greaterThan && valueToCompare >= value) {
                return true;
            }

            if (!greaterThan && valueToCompare <= value) {
                return true;
            }
        }

        return false;
    }

    public boolean matchExpiryDateParameters(IdentityType storedEntry) {
        if (identityQuery.getParameter(IdentityType.EXPIRY_DATE) != null
                || identityQuery.getParameter(IdentityType.EXPIRY_BEFORE) != null
                || identityQuery.getParameter(IdentityType.EXPIRY_AFTER) != null) {
            Date expiryDate = storedEntry.getExpirationDate();

            if (!isQueryParameterEquals(identityQuery, IdentityType.EXPIRY_DATE, expiryDate)) {
                return false;
            }

            Long expiryDateInMillis = null;

            if (expiryDate != null) {
                expiryDateInMillis = expiryDate.getTime();
            }

            if (!isQueryParameterLessThan(identityQuery, IdentityType.EXPIRY_BEFORE, expiryDateInMillis)) {
                return false;
            }

            if (!isQueryParameterGreaterThan(identityQuery, IdentityType.EXPIRY_AFTER, expiryDateInMillis)) {
                return false;
            }
        }

        return true;
    }

}