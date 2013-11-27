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

package org.picketlink.idm.spi;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.acl.spi.PermissionHandlerPolicy;

/**
 * Stores security related state for one or more identity management operations
 *
 * @author Shane Bryzak
 *
 */
public interface IdentityContext {

    /**
     * Returns the parameter value with the specified name
     *
     * @return
     */
    <P> P getParameter(String paramName);

    /**
     * Returns a boolean indicating whether the parameter with the specified name has been set
     *
     * @param paramName
     * @return
     */
    boolean isParameterSet(String paramName);

    /**
     * Sets a parameter value
     *
     * @param paramName
     * @param value
     */
    void setParameter(String paramName, Object value);

    /**
     *
     * @return
     */
    EventBridge getEventBridge();

    /**
     *
     * @return
     */
    IdGenerator getIdGenerator();

    /**
     * Return the active Partition for this context
     *
     * @return
     */
    Partition getPartition();

    /**
     * Return the permission handler policy (used for permission related operations)
     *
     * @return
     */
    PermissionHandlerPolicy getPermissionHandlerPolicy();
}