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

import org.picketlink.idm.model.Group;
import org.picketlink.idm.model.SimpleGroup;

/**
 * @author Pedro Silva
 * 
 */
public class FileGroup extends AbstractIdentityTypeEntry<Group> {

    private static final long serialVersionUID = 1L;

    private static final transient String FILE_Role_VERSION = "1";

    public FileGroup(Group group) {
        super(FILE_Role_VERSION, group);
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        super.doPopulateProperties(properties);
        
        Group group = getEntry();
        
        properties.put("name", group.getName());
        
        if (group.getParentGroup() != null) {
            properties.put("parentId", group.getParentGroup().getId());
            properties.put("parentName", group.getParentGroup().getName());
        }
    }
    


    @Override
    protected Group doCreateInstance(Map<String, Serializable> properties) throws Exception {
        Group parent = null;
        
        if (properties.get("parentId") != null) {
            parent = new SimpleGroup(properties.get("parentName").toString());
            
            parent.setId(properties.get("parentId").toString());
        }

        SimpleGroup group = null;
        
        String name = properties.get("name").toString();
        
        if (parent != null) {
            group = new SimpleGroup(name, parent);
        } else {
            group = new SimpleGroup(name);
        }
        
        
        return group;
    }
    
}
