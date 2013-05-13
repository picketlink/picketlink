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

import java.io.Serializable;

/**
 * A PermissionResolver may be used to determine access restrictions for application resources. For every
 * permission check the application performs, the hasPermission() method of each known PermissionResolver 
 * is invoked. For the permission check to succeed, at least one PermissionResolver must return a result of 
 * PermissionStatus.ALLOW.  If any PermissionResolver returns a result of PermissionStatus.DENY, the 
 * permission check is unsuccessful and the user is not allowed to carry out the requested operation.  
 * If a PermissionResolver does not explicitly allow or deny the permission, it should return a result of 
 * PermissionStatus.NOT_APPLICABLE.
 * 
 * @author Shane Bryzak
 *
 */
public interface PermissionResolver
{
    public enum PermissionStatus {
        ALLOW, DENY, NOT_APPLICABLE
    }

    PermissionStatus hasPermission(Object resource, String operation);

    PermissionStatus hasPermission(Class<?> resourceClass, Serializable identifier, String operation);
}
