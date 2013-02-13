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

package org.picketlink.permission;

import java.util.List;
import java.util.Set;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.permission.spi.PermissionStore;

/**
 * API for querying object permissions
 * 
 * @author Shane Bryzak
 *
 */
public class PermissionQuery
{
    private Object resource;    
    private Set<Object> resources;
    private int offset;
    private int limit;
    private IdentityType recipient;
    
    private PermissionStore permissionStore;
    
    public PermissionQuery(PermissionStore permissionStore)
    {
        this.permissionStore = permissionStore;
    }
    
    public Object getResource()
    {
        return resource;
    }
    
    public PermissionQuery setResource(Object resource)
    {
        this.resource = resource;
        this.resources = null;
        return this;
    }
    
    public Set<Object> getResources()
    {
        return resources;
    }
    
    public PermissionQuery setResources(Set<Object> resources)
    {
        this.resources = resources;
        this.resource = null;
        return this;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public PermissionQuery setOffset(int offset) {
        this.offset = offset;
        return this;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public PermissionQuery setLimit(int limit) {
        this.limit = limit;
        return this;
    }
    
    public IdentityType getRecipient()
    {
        return recipient;
    }
    
    public PermissionQuery setRecipient(IdentityType recipient)
    {
        this.recipient = recipient;
        return this;
    }
    
    public List<Permission> getResultList() 
    {
        return permissionStore.getPermissions(this);
    }    
}
