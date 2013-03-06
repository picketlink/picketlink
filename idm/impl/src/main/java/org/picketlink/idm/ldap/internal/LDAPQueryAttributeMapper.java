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

import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.QueryParameter;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class LDAPQueryAttributeMapper {

    private static final Map<QueryParameter, String> customAttributeMap = new HashMap<QueryParameter, String>();
    private static final Map<QueryParameter, String> ldapAttributeMap = new HashMap<QueryParameter, String>();

    static {
        ldapAttributeMap.put(User.ID, LDAPConstants.ENTRY_UUID);
        ldapAttributeMap.put(User.LOGIN_NAME, LDAPConstants.UID);
        ldapAttributeMap.put(User.EMAIL, LDAPConstants.EMAIL);
        ldapAttributeMap.put(User.FIRST_NAME, LDAPConstants.GIVENNAME);
        ldapAttributeMap.put(User.LAST_NAME, LDAPConstants.SN);
        ldapAttributeMap.put(User.MEMBER_OF, LDAPConstants.MEMBER_OF);
        ldapAttributeMap.put(Role.NAME, LDAPConstants.CN);
        ldapAttributeMap.put(Group.NAME, LDAPConstants.CN);
        ldapAttributeMap.put(User.CREATED_DATE, LDAPConstants.CREATE_TIMESTAMP);
        ldapAttributeMap.put(User.CREATED_BEFORE, LDAPConstants.CREATE_TIMESTAMP);
        ldapAttributeMap.put(User.CREATED_AFTER, LDAPConstants.CREATE_TIMESTAMP);

        customAttributeMap.put(User.EXPIRY_DATE, LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE);
        customAttributeMap.put(User.EXPIRY_BEFORE, LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE);
        customAttributeMap.put(User.EXPIRY_AFTER, LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE);
        customAttributeMap.put(IdentityType.ENABLED, LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED);
    }

    public static Attribute map(QueryParameter parameter) {
        String attribute = ldapAttributeMap.get(parameter);

        if (attribute == null) {
            return null;
        }

        return new BasicAttribute(attribute);
    }

    public static Attribute mapCustom(QueryParameter queryParameter) {
        if (customAttributeMap.get(queryParameter) == null) {
            return null;
        }

        return new BasicAttribute(customAttributeMap.get(queryParameter));
    }

}
