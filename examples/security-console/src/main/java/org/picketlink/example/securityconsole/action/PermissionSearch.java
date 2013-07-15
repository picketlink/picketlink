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

package org.picketlink.example.securityconsole.action;

import java.util.Arrays;
import java.util.List;
import javax.ejb.Stateful;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.picketlink.example.securityconsole.model.Customer;
import org.picketlink.permission.Permission;

@Stateful
@Model
public class PermissionSearch 
{
    private static final List<String> ENTITY_TYPES = Arrays.asList(new String[] {"Customer", "Project"});
    
    private String entityType = ENTITY_TYPES.get(0);
    
    @PersistenceContext
    private EntityManager em;
    
    private Object resource;
        
    private List<Permission> permissions;
    
    @Inject
    private PermissionManager permissionManager;
    
    public String getEntityType()
    {
        return entityType;
    }
    
    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }
    
    public List<String> getEntityTypes()
    {
        return ENTITY_TYPES;
    }
    
    public List<Customer> getCustomers()
    {
        return em.createQuery("select C from Customer C").getResultList();
    }
    
    public Object getResource()
    {
        return resource;
    }
    
    public void setResource(Object resource)
    {
        this.resource = resource;
    }
    
    public List<Permission> getPermissions()
    {
        if (permissions == null && resource != null)
        {
            permissions = permissionManager.createPermissionQuery()
                    .setResource(resource)
                    .getResultList();
            
        }
        return permissions;
    }
}
