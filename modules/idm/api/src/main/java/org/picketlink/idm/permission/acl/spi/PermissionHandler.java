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

package org.picketlink.idm.permission.acl.spi;

import java.io.Serializable;
import java.util.Set;


/**
 * Handles the generation of permission resource identifiers, and is responsible for the
 * marshaling / unmarshaling of permissions
 *
 * @author Shane Bryzak
 */
public interface PermissionHandler {
    /**
     * Returns true if the implementation can handle resources of the specified class
     *
     * @param resourceClass
     * @return
     */
    boolean canHandle(Class<?> resourceClass);

    /**
     * Returns a Serializable identifier value that can be used to uniquely identify the specified resource
     *
     * @param resource
     * @return
     */
    Serializable getIdentifier(Object resource);

    /**
     * Returns the formal class of the specified resource
     *
     * @param resource
     * @return
     */
    Class<?> unwrapResourceClass(Object resource);

    /**
     * Returns a set containing the available permissions for a resource class.  If there are no hard coded
     * permissions defined (i.e. any permission is allowed) then this method must return an empty set.
     *
     * @param resourceClass
     * @return
     */
    Set<String> listClassOperations(Class<?> resourceClass);

    /**
     * Returns a set containing the available permissions for a particular resource instance.  If there are no hard coded
     * permissions defined (i.e. any permission is allowed) then this method must return an empty set.
     *
     * @param resourceClass
     * @return
     */
    Set<String> listInstanceOperations(Class<?> resourceClass);

}
