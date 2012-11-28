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

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.Role;

/**
 * Implementation of {@link Role} for storage in ldap
 *
 * @author anil saldhana
 * @since Aug 31, 2012
 */
public class LDAPRole extends DirContextAdaptor implements Role {

    private static final long serialVersionUID = 1L;
    
    private String roleName, roleDNSuffix;

    public LDAPRole() {
        Attribute oc = new BasicAttribute(OBJECT_CLASS);
        oc.add("top");
        oc.add(LDAPConstants.GROUP_OF_NAMES);
        getLDAPAttributes().put(oc);
    }

    /**
     * <p>
     * Creates a {@link LDAPRole} with the provided LDAP {@link Attributes}. The <code>roleDNSuffix</code> defines where roles
     * entries reside in the LDAP tree.
     * </p>
     *
     * @param ldapAttributes
     */
    public LDAPRole(Attributes ldapAttributes, String roleDNSuffix) {
        setLDAPAttributes(ldapAttributes);
        this.roleDNSuffix = roleDNSuffix;
    }

    public void setRoleDNSuffix(String rdns) {
        this.roleDNSuffix = rdns;
    }

    public String getDN() {
        return CN + EQUAL + getAttribute(CN) + COMMA + roleDNSuffix;
    }

    public void setName(String roleName) {
        this.roleName = roleName;
        Attribute theAttribute = getLDAPAttributes().get(CN);
        if (theAttribute == null) {
            getLDAPAttributes().put(CN, roleName);
        } else {
            theAttribute.set(0, roleName);
        }
        getLDAPAttributes().put(MEMBER, SPACE_STRING); // Dummy member for now
    }

    @Override
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

    public void addUser(String userDN) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);
        if (memberAttribute != null) {
            if (memberAttribute.contains(SPACE_STRING)) {
                memberAttribute.remove(SPACE_STRING);
            }
        } else {
            memberAttribute = new BasicAttribute(OBJECT_CLASS);
            memberAttribute.add("inetOrgPerson");
            memberAttribute.add("organizationalPerson");
            memberAttribute.add("person");
            memberAttribute.add("top");
        }
        memberAttribute.add(userDN);
    }

    public void removeUser(String userDN) {
        Attribute memberAttribute = getLDAPAttributes().get(MEMBER);
        if (memberAttribute != null) {
            memberAttribute.remove(userDN);
        }
    }
}