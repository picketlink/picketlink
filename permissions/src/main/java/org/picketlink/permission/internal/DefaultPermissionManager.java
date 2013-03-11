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

package org.picketlink.permission.internal;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.picketlink.permission.Permission;
import org.picketlink.permission.PermissionManager;
import org.picketlink.permission.PermissionQuery;
import org.picketlink.permission.spi.PermissionStore;

/**
 * Default implementation of the PermissionManager interface
 */
@ApplicationScoped
public class DefaultPermissionManager implements PermissionManager
{
    @Inject
    PermissionStore permissionStore;

    @Override
    public PermissionQuery createPermissionQuery()
    {
        PermissionQuery q = new PermissionQuery(permissionStore);
        return q;
    }

    @Override
    public void grantPermission(Permission permission)
    {
        permissionStore.grantPermission(permission);        
    }

    @Override
    public void grantPermissions(Collection<Permission> permission)
    {
        permissionStore.grantPermissions(permission);
    }

    @Override
    public void revokePermission(Permission permission)
    {
        permissionStore.revokePermission(permission);        
    }

    @Override
    public void revokePermissions(Collection<Permission> permissions)
    {
        permissionStore.revokePermissions(permissions);        
    }

}
