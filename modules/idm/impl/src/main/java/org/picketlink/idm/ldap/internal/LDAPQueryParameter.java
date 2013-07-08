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

package org.picketlink.idm.ldap.internal;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.QueryParameter;

import javax.naming.directory.Attribute;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
        Attribute mapped = LDAPQueryAttributeMapper.map(queryParameter);

        if (mapped == null) {
            mapped = LDAPQueryAttributeMapper.mapCustom(getQueryParameter());
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
        return LDAPQueryAttributeMapper.map(getQueryParameter()) != null && !isMembershipParameter();
    }

    public boolean isMembershipParameter() {
//        return queryParameter.equals(IdentityType.HAS_ROLE) || queryParameter.equals(IdentityType.MEMBER_OF)
//                || queryParameter.equals(IdentityType.HAS_GROUP_ROLE) || queryParameter.equals(org.picketlink.idm.model.sample.Role.ROLE_OF) || queryParameter.equals(org.picketlink.idm.model.sample.Group.HAS_MEMBER);
        return false;
    }

    public String createFilter() {
        if (getValues().length == 0 || !isMappedToManagedAttribute()) {
            return null;
        }

        String filter = "(&";

        for (Object value : getValues()) {
            if (Date.class.isInstance(value)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss'Z'");

                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                value = sdf.format(((Date) value));
            }

            if (this.queryParameter.equals(IdentityType.CREATED_AFTER)) {
                filter = filter + "(" + getMappedTo().getID() + ">=" + value.toString() + ")";
            } else if (this.queryParameter.equals(IdentityType.CREATED_BEFORE)) {
                filter = filter + "(" + getMappedTo().getID() + "<=" + value.toString() + ")";
            } else {
                filter = filter + "(" + getMappedTo().getID() + "=" + value.toString() + ")";
            }

        }

        filter = filter + ")";

        return filter;
    }

}