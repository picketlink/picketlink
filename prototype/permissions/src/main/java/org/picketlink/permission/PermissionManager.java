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

import java.util.Collection;


/**
 * Manages user, role and group permissions. 
 * 
 * @author Shane Bryzak
 *
 */
public interface PermissionManager
{
    /**
     * 
     * @return A new PermissionQuery
     */
    PermissionQuery createPermissionQuery();
    
    /**
     * 
     * @param permission
     */
    void grantPermission(Permission permission);
    
    /**
     * 
     * @param permission
     */
    void grantPermissions(Collection<Permission> permission);
    
    /**
     * 
     * @param permission
     */
    void revokePermission(Permission permission);
    
    /**
     * 
     * @param permissions
     */
    void revokePermissions(Collection<Permission> permissions);
}
