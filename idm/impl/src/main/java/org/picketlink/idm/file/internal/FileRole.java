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
import java.util.Map;

import org.jboss.security.identity.plugins.SimpleRole;
import org.picketlink.idm.model.Role;

/**
 * @author Pedro Silva
 * 
 */
public class FileRole extends AbstractIdentityTypeEntry<Role> {

    private static final long serialVersionUID = 1L;

    private static final transient String FILE_Role_VERSION = "1";

    public FileRole(Role role) {
        super(FILE_Role_VERSION, role);
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);
        
        Role role = getEntry();
        
        properties.put("name", role.getName());
    }

    @Override
    protected Role doCreateInstance(Map<String, Serializable> properties) throws Exception {
        String name = properties.get("name").toString(); 
        return (Role) new SimpleRole(name);
    }
    
}
