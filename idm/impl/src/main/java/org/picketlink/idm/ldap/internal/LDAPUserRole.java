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

import static org.picketlink.idm.ldap.internal.LDAPConstants.OBJECT_CLASS;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * Attributes of an {@link LDAPUser} that does not map to LDAP managed attributes
 *
 * @author anil saldhana
 * @since Sep 7, 2012
 */
public class LDAPUserRole extends LDAPEntry {

    private static final long serialVersionUID = 1L;
    
    private LDAPUser user;
    private LDAPRole role;

    public LDAPUserRole(LDAPUser user, LDAPRole role) {
        super(user.getDN());
        this.user = user;
        this.role = role;
        Attribute oc = new BasicAttribute(OBJECT_CLASS);
        oc.add("top");
        oc.add(LDAPConstants.GROUP_OF_NAMES);
        
        getLDAPAttributes().put(oc);
        
        getLDAPAttributes().put(LDAPConstants.MEMBER, this.role.getDN());
    }
    
    @Override
    public String getDN() {
        return "cn=roles" + LDAPConstants.COMMA + this.user.getDN(); 
    }
    
    public User getMember() {
        return this.user;
    }

    public Role getRole() {
        return this.role;
    }

    @Override
    public String getKey() {
        return null;
    }

}