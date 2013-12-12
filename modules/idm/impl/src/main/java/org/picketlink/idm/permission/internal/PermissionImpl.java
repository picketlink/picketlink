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

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.Permission;

/**
 * Default Permission implementation
 *
 * @author Shane Bryzak
 */
public class PermissionImpl implements Permission {
    private Object resource;

    private Class<?> resourceClass;

    private Serializable resourceIdentifier;

    private IdentityType assignee;

    private String operation;

    public PermissionImpl(Object resource, IdentityType assignee, String operation) {
        this.resource = resource;
        this.assignee = assignee;
        this.operation = operation;
    }

    public PermissionImpl(Class<?> resourceClass, Serializable resourceIdentifier, IdentityType assignee, String operation) {
        this.resourceClass = resourceClass;
        this.resourceIdentifier = resourceIdentifier;
        this.assignee = assignee;
        this.operation = operation;
    }

    @Override
    public Object getResource() {
        return resource;
    }

    @Override
    public Class<?> getResourceClass() {
        return resourceClass;
    }

    @Override
    public Serializable getResourceIdentifier() {
        return resourceIdentifier;
    }

    @Override
    public IdentityType getAssignee() {
        return assignee;
    }

    @Override
    public String getOperation() {
        return operation;
    }
}
