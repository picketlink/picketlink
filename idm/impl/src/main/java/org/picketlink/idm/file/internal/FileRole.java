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

import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;

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
        return new SimpleRole(name);
    }
    
}
