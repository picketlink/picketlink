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

package org.picketlink.idm;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.User;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class DefaultIdentityCache implements IdentityCache {

    @Override
    public User lookupUser(Realm realm, String id) {
        return null;
    }

    @Override
    public Group lookupGroup(Partition partition, String groupId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Role lookupRole(Partition partition, String name) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putUser(Realm realm, User user) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void putGroup(Partition partition, Group group) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void putRole(Partition partition, Role role) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Agent lookupAgent(Realm realm, String id) {
        return null;
    }

    @Override
    public void putAgent(Realm realm, Agent agent) {
        
    }

}