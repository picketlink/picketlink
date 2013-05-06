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

import java.io.Serializable;

import org.picketlink.permission.PermissionResolver;

/**
 * A PermissionResolver implementation that provides ACL-style object permissions, backed by a database.
 *
 */
public class PersistentPermissionResolver implements PermissionResolver
{

    public PermissionStatus hasPermission(Object resource, String operation)
    {
        return null;
    }


    public PermissionStatus hasPermission(Class<?> resourceClass, Serializable identifier, String operation)
    {
        return null;
    }
}
