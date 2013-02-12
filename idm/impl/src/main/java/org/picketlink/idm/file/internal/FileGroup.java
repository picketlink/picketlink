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
