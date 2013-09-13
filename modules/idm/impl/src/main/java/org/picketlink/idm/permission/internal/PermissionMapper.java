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

package org.picketlink.idm.permission.internal;

import java.io.Serializable;
import java.util.List;

import org.picketlink.idm.permission.PermissionResolver;
import org.picketlink.idm.permission.PermissionResolver.PermissionStatus;

/**
 * Iterates through the available PermissionResolver instances to determine whether an application permission
 * is to be allowed or denied.
 *
 * @author Shane Bryzak
 */
public class PermissionMapper {
    private List<PermissionResolver> resolvers;

    public boolean resolvePermission(Object resource, String operation) {
        boolean permit = false;

        for (PermissionResolver resolver : resolvers) {
            PermissionStatus status = resolver.hasPermission(resource, operation);
            if (PermissionStatus.ALLOW.equals(status)) {
                permit = true;
            }
            else if (PermissionStatus.DENY.equals(status)) {
                return false;
            }
        }

        return permit;
    }

    public boolean resolvePermission(Class<?> resourceClass, Serializable identifier, String operation) {
        boolean permit = false;

        for (PermissionResolver resolver : resolvers) {
            PermissionStatus status = resolver.hasPermission(resourceClass, identifier, operation);
            if (PermissionStatus.ALLOW.equals(status)) {
                permit = true;
            }
            else if (PermissionStatus.DENY.equals(status)) {
                return false;
            }
        }

        return permit;
    }
}
