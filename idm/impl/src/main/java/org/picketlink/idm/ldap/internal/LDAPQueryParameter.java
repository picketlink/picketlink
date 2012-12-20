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

import javax.naming.directory.Attribute;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class LDAPQueryParameter {

    private QueryParameter queryParameter;
    private Object[] values;

    public LDAPQueryParameter(QueryParameter queryParameter, Object[] values) {
        this.queryParameter = queryParameter;
        this.values = values;
    }

    public QueryParameter getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(QueryParameter queryParameter) {
        this.queryParameter = queryParameter;
    }

    public Attribute getMappedTo() {
        Attribute mapped = LDAPAttributeMapper.map(queryParameter);

        if (mapped == null) {
            mapped = LDAPAttributeMapper.mapCustom(getQueryParameter());
        }

        return mapped;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public boolean isMappedToManagedAttribute() {
        return LDAPAttributeMapper.map(getQueryParameter()) != null && !isMembershipParameter();
    }

    public boolean isMembershipParameter() {
        return queryParameter.equals(IdentityType.HAS_ROLE) || queryParameter.equals(IdentityType.MEMBER_OF)
                || queryParameter.equals(IdentityType.HAS_GROUP_ROLE) || queryParameter.equals(Role.ROLE_OF) || queryParameter.equals(Group.HAS_MEMBER);
    }

    public String createFilter() {
        if (getValues().length == 0 || !isMappedToManagedAttribute()) {
            return null;
        }

        String filter = "(&";

        for (Object value : getValues()) {
            filter = filter + "(" + getMappedTo().getID() + "=" + value.toString() + ")";
        }

        filter = filter + ")";

        return filter;
    }

}