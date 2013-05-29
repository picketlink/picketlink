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
import javax.naming.directory.BasicAttribute;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class LDAPQueryAttributeMapper {

    private static final Map<QueryParameter, String> customAttributeMap = new HashMap<QueryParameter, String>();
    private static final Map<QueryParameter, String> ldapAttributeMap = new HashMap<QueryParameter, String>();

    static {
        ldapAttributeMap.put(org.picketlink.idm.model.User.ID, LDAPConstants.ENTRY_UUID);
        ldapAttributeMap.put(org.picketlink.idm.model.User.LOGIN_NAME, LDAPConstants.UID);
        ldapAttributeMap.put(org.picketlink.idm.model.User.EMAIL, LDAPConstants.EMAIL);
        ldapAttributeMap.put(org.picketlink.idm.model.User.FIRST_NAME, LDAPConstants.GIVENNAME);
        ldapAttributeMap.put(org.picketlink.idm.model.User.LAST_NAME, LDAPConstants.SN);
        ldapAttributeMap.put(org.picketlink.idm.model.User.MEMBER_OF, LDAPConstants.MEMBER_OF);
        ldapAttributeMap.put(org.picketlink.idm.model.Role.NAME, LDAPConstants.CN);
        ldapAttributeMap.put(org.picketlink.idm.model.Group.NAME, LDAPConstants.CN);
        ldapAttributeMap.put(org.picketlink.idm.model.User.CREATED_DATE, LDAPConstants.CREATE_TIMESTAMP);
        ldapAttributeMap.put(org.picketlink.idm.model.User.CREATED_BEFORE, LDAPConstants.CREATE_TIMESTAMP);
        ldapAttributeMap.put(org.picketlink.idm.model.User.CREATED_AFTER, LDAPConstants.CREATE_TIMESTAMP);

        customAttributeMap.put(org.picketlink.idm.model.User.EXPIRY_DATE, LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE);
        customAttributeMap.put(org.picketlink.idm.model.User.EXPIRY_BEFORE, LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE);
        customAttributeMap.put(org.picketlink.idm.model.User.EXPIRY_AFTER, LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE);
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
