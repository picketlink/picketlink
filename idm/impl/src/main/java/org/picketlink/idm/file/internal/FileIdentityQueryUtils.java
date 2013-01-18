/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.file.internal;

import java.io.Serializable;
import java.util.Date;

import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author Pedro Silva
 *
 */
public final class FileIdentityQueryUtils {

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

    public static boolean isQueryParameterEquals(IdentityQuery<?> identityQuery, QueryParameter queryParameter, Date valueToCompare) {
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

    public static boolean isQueryParameterLessThan(IdentityQuery<?> identityQuery, QueryParameter queryParameter, Long valueToCompare) {
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
    
}
