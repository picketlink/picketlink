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

package org.picketlink.idm.file.internal;

import java.io.Serializable;

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Membership;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class FileMembership implements Membership, Serializable {

    private static final long serialVersionUID = -7424412115619080350L;
    
    private User user;
    private Group group;
    private Role role;

    public FileMembership() {
        
    }
    
    public FileMembership(Role role, User user, Group group) {
        this.role = role;
        this.user = user;
        this.group = group;
    }

    /* (non-Javadoc)
     * @see org.picketlink.idm.model.Membership#getUser()
     */
    @Override
    public User getUser() {
        return this.user;
    }

    /* (non-Javadoc)
     * @see org.picketlink.idm.model.Membership#getGroup()
     */
    @Override
    public Group getGroup() {
        return this.group;
    }

    /* (non-Javadoc)
     * @see org.picketlink.idm.model.Membership#getRole()
     */
    @Override
    public Role getRole() {
        return this.role;
    }

}
