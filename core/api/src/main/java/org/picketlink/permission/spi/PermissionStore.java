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

package org.picketlink.permission.spi;

import java.util.Collection;
import java.util.List;

import org.picketlink.permission.Permission;
import org.picketlink.permission.PermissionQuery;

/**
 * 
 * @author Shane Bryzak
 */
public interface PermissionStore
{
    List<Permission> getPermissions(PermissionQuery query);

    boolean grantPermission(Permission permission);

    boolean grantPermissions(Collection<Permission> permissions);

    boolean revokePermission(Permission permission);

    boolean revokePermissions(Collection<Permission> permissions);

    List<String> listAvailableActions(Object target);

    void clearPermissions(Object target);

    boolean isEnabled();
}
