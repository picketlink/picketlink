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

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Role;

/**
 * Attributes of an {@link LDAPUser} that does not map to LDAP managed attributes
 *
 * @author anil saldhana
 * @since Sep 7, 2012
 */
public class LDAPGroupRole extends LDAPEntry implements GroupRole {

    private static final long serialVersionUID = 1L;
    
    private LDAPUser user;
    private LDAPGroup group;
    private LDAPRole role;

    public LDAPGroupRole(LDAPUser user, LDAPGroup group, LDAPRole role) {
        super(user.getDN());
        this.user = user;
        this.group = group;
        this.role = role;
        Attribute oc = new BasicAttribute(OBJECT_CLASS);
        oc.add("top");
        oc.add(LDAPConstants.GROUP_OF_NAMES);
        
        getLDAPAttributes().put(oc);
        
        getLDAPAttributes().put(LDAPConstants.MEMBER, this.role.getDN());
        
//        Attribute theAttribute = getLDAPAttributes().get(LDAPConstants.CN);
//        
//        if (theAttribute == null) {
//            getLDAPAttributes().put(LDAPConstants.CN, this.group.getName());
//        } else {
//            theAttribute.set(0, this.group.getName());
//        }

    }
    
    @Override
    public String getDN() {
        return "cn=" + this.group.getName() + LDAPConstants.COMMA + this.user.getDN(); 
    }
    
    @Override
    public IdentityType getMember() {
        return this.user;
    }

    @Override
    public Group getGroup() {
        return this.group;
    }

    @Override
    public Role getRole() {
        return this.role;
    }

    @Override
    public String getKey() {
        return null;
    }   
    
}