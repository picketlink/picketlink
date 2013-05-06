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


import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.ldap.internal.LDAPConstants.CN;
import static org.picketlink.idm.ldap.internal.LDAPConstants.MEMBER;
import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;
import static org.picketlink.idm.ldap.internal.LDAPConstants.SPACE_STRING;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.Group;

/**
 * LDAP Representation of the {@link Group}
 *
 * @author anil saldhana
 * @since Sep 4, 2012
 */
public class LDAPGroup extends LDAPIdentityType implements Group {

    private static final long serialVersionUID = 1L;

    private Group parent;
    private String groupName;

    private String path;

    public LDAPGroup(String groupDNSuffix) {
        super(groupDNSuffix);
        Attribute oc = new BasicAttribute(OBJECT_CLASS);
        oc.add("top");
        oc.add("groupOfNames");
        getLDAPAttributes().put(oc);
    }

    public LDAPGroup(String name, String groupDNSuffix) {
        this(groupDNSuffix);

        if (name == null) {
            throw MESSAGES.nullArgument("Name");
        }

        setName(name);
    }

    public void setName(String name) {
        this.groupName = name;
        Attribute theAttribute = getLDAPAttributes().get(CN);
        if (theAttribute == null) {
            getLDAPAttributes().put(CN, groupName);
        } else {
            theAttribute.set(0, groupName);
        }
        getLDAPAttributes().put(MEMBER, SPACE_STRING); // Dummy member for now
    }

    @Override
    public String getName() {
        if (groupName == null) {
            Attribute cnAttribute = getLDAPAttributes().get(CN);
            if (cnAttribute != null) {
                try {
                    groupName = (String) cnAttribute.get();
                } catch (NamingException ignore) {
                }
            }
        }
        return groupName;
    }

    public void setParentGroup(Group parent) {
        this.parent = parent;
    }

    @Override
    public Group getParentGroup() {
        return parent;
    }

    public void addMember(LDAPGroup childGroup) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);

        if (memberAttribute == null) {
            memberAttribute = new BasicAttribute(MEMBER);
            getLDAPAttributes().put(memberAttribute);
        }

        memberAttribute.add(childGroup.getDN());
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public void setPath(String groupPath) {
        this.path = groupPath;
    }

}