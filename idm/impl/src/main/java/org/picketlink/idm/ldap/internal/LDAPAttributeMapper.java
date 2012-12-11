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
public class LDAPAttributeMapper {

    private static final Map<QueryParameter, String> customAttributeMap = new HashMap<QueryParameter, String>();
    private static final Map<QueryParameter, String> ldapAttributeMap = new HashMap<QueryParameter, String>();
    
    static {
        ldapAttributeMap.put(User.ID, LDAPConstants.UID);
        ldapAttributeMap.put(User.EMAIL, LDAPConstants.EMAIL);
        ldapAttributeMap.put(User.FIRST_NAME, LDAPConstants.GIVENNAME);
        ldapAttributeMap.put(User.LAST_NAME, LDAPConstants.SN);
        ldapAttributeMap.put(User.MEMBER_OF, LDAPConstants.MEMBER_OF);
        
        ldapAttributeMap.put(Role.NAME, LDAPConstants.CN);
        
        ldapAttributeMap.put(Group.NAME, LDAPConstants.CN);
        
        customAttributeMap.put(User.CREATED_DATE, LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE);
        customAttributeMap.put(User.CREATED_BEFORE, LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE);
        customAttributeMap.put(User.CREATED_AFTER, LDAPConstants.CUSTOM_ATTRIBUTE_CREATE_DATE);
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
