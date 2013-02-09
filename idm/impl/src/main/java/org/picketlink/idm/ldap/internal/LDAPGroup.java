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
            throw new IllegalArgumentException("You must provide a name.");
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

    public void addChildGroup(LDAPGroup childGroup) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);
        
        if (memberAttribute == null) {
            memberAttribute = new BasicAttribute(MEMBER);
            getLDAPAttributes().put(memberAttribute);
        }
        
        memberAttribute.add(getDN(childGroup.getName()));
    }

    @Override
    public String getPath() {
        return this.path;
    }

    public void setPath(String groupPath) {
        this.path = groupPath;
    }

}