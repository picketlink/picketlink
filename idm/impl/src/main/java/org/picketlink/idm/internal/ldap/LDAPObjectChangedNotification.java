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
package org.picketlink.idm.internal.ldap;

import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;

/**
 * Indicates that an LDAP Object has changed
 *
 * @author anil saldhana
 * @since Sep 4, 2012
 */
public class LDAPObjectChangedNotification {
    private DirContext object;
    private NType ntype;
    private Attribute attribute;

    /**
     * Notification Type
     */
    public enum NType {
        ADD_ATTRIBUTE, REMOVE_ATTRIBUTE, CUSTOM_ATTRIBUTE, REPLACE_ATTRIBUTE
    };

    public LDAPObjectChangedNotification(DirContext ldapObject, NType nt, Attribute attribute) {
        this.object = ldapObject;
        this.ntype = nt;
        this.attribute = attribute;
    }

    public DirContext getLDAPObject() {
        return object;
    }

    public NType getNtype() {
        return ntype;
    }

    public Attribute getAttribute() {
        return attribute;
    }
}