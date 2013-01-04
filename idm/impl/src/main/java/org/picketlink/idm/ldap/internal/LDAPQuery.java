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

package org.picketlink.idm.ldap.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class LDAPQuery {

    private List<LDAPQueryParameter> managedParameters = new ArrayList<LDAPQueryParameter>();
    private Boolean hasCustomAttributes = null;

    public LDAPQuery(Map<QueryParameter, Object[]> queryParameters) {
        for (Entry<QueryParameter, Object[]> entry : queryParameters.entrySet()) {
            QueryParameter queryParameter = entry.getKey();
            Object[] values = entry.getValue();

            LDAPQueryParameter parameter = new LDAPQueryParameter(queryParameter, values);

            if (parameter.isMappedToManagedAttribute()) {
                this.managedParameters.add(parameter);
            } else if (!parameter.isMembershipParameter()) {
                this.hasCustomAttributes = true;
            }
        }
    }

    public StringBuffer createManagedAttributesFilter() {
        if (getManagedParameters().isEmpty()) {
            return null;
        }

        StringBuffer filter = new StringBuffer("(&(objectClass=*)");

        for (LDAPQueryParameter ldapQueryParameter : getManagedParameters()) {
            filter.append(ldapQueryParameter.createFilter());
        }

        filter.append(")");

        return filter;
    }

    public boolean hasCustomAttributes() {
        return this.hasCustomAttributes != null && this.hasCustomAttributes;
    }

    public List<LDAPQueryParameter> getManagedParameters() {
        return this.managedParameters;
    }

}
