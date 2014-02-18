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

package org.picketlink.idm.permission;

import java.io.Serializable;

/**
 * Abstract base class representing a specific permission granted for a domain resource.  If the actual resource object instance is known
 * then the getResource() method will return a reference to it, otherwise the getResourceClass() and getResourceIdentifier()
 * methods may be used to determine the specific resource that the permission applies to.
 *
 * It is the responsibility of any subclasses to declare any logic relating to the assignee of the Permission.
 *
 * @author Shane Bryzak
 */
public abstract class Permission {
    private Object resource;

    private Class<?> resourceClass;

    private Serializable resourceIdentifier;

    private String operation;

    public Permission(Object resource, String operation) {
        this.resource = resource;
        this.operation = operation;
    }

    public Permission(Class<?> resourceClass, Serializable resourceIdentifier, String operation) {
        this.resourceClass = resourceClass;
        this.resourceIdentifier = resourceIdentifier;
        this.operation = operation;
    }

    /**
     * Returns the resource object if known, otherwise returns null.  If the resource object is not known, then the
     * getResourceClass() and getResourceIdentifier() methods represent the "coordinates" of the resource.
     *
     * @return Object The resource instance, or null
     */
    public Object getResource() {
        return resource;
    }

    /**
     * Returns the resource class if the actual resource instance is not known, otherwise returns null.
     *
     * @return
     */
    public Class<?> getResourceClass() {
        return resourceClass;
    }

    /**
     * Returns the resource identifier if the actual resource instance is not known, otherwise returns null.
     *
     * @return
     */
    public Serializable getResourceIdentifier() {
        return resourceIdentifier;
    }

    /**
     * Returns the permission operation
     *
     * @return
     */
    public String getOperation() {
        return operation;
    }
}
