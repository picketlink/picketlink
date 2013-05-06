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

import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SPACE_STRING;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.Role;

/**
 * Implementation of {@link Role} for storage in ldap
 *
 * @author anil saldhana
 * @since Aug 31, 2012
 */ // FIXME
public class LDAPRole extends LDAPIdentityType implements Role {

    private static final long serialVersionUID = 1L;

    private String roleName;

    public LDAPRole(String roleDNSuffix) {
        super(roleDNSuffix);

        Attribute oc = new BasicAttribute(OBJECT_CLASS);
        oc.add("top");
        oc.add(LDAPConstants.GROUP_OF_NAMES);
        getLDAPAttributes().put(oc);
    }

    public LDAPRole(String name, String roleDNSuffix) {
        this(roleDNSuffix);

        if (name == null) {
            throw new IllegalArgumentException("You must provide a name.");
        }

        setName(name);
    }

    public void setName(String roleName) {
        this.roleName = roleName;
        Attribute theAttribute = getLDAPAttributes().get(CN);
        if (theAttribute == null) {
            getLDAPAttributes().put(CN, roleName);
        } else {
            theAttribute.set(0, roleName);
        }
        getLDAPAttributes().put(MEMBER, SPACE_STRING);
    }

    //@Override
    public String getName() {
        if (roleName == null) {
            Attribute cnAttribute = getLDAPAttributes().get(CN);
            if (cnAttribute != null) {
                try {
                    roleName = (String) cnAttribute.get();
                } catch (NamingException ignore) {
                }
            }
        }
        return roleName;
    }

}