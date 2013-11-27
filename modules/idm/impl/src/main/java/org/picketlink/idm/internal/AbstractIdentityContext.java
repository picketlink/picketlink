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
package org.picketlink.idm.internal;

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.permission.acl.spi.PermissionHandlerPolicy;
import org.picketlink.idm.spi.IdentityContext;

/**
 * Abstract base class for creating IdentityContext implementations.
 *
 * Subclasses extending this class are not thread-safe!
 *
 * @author Shane Bryzak
 */
public abstract class AbstractIdentityContext implements IdentityContext {

    private final Partition partition;
    private final EventBridge eventBridge;
    private final IdGenerator idGenerator;
    private final PermissionHandlerPolicy permissionHandlerPolicy;

    public AbstractIdentityContext(Partition partition, EventBridge eventBridge, IdGenerator idGenerator) {
        this(partition, eventBridge, idGenerator, null);
    }

    public AbstractIdentityContext(Partition partition, EventBridge eventBridge, IdGenerator idGenerator,
            PermissionHandlerPolicy permissionHandlerPolicy) {
        this.partition = partition;
        this.eventBridge = eventBridge;
        this.idGenerator = idGenerator;
        this.permissionHandlerPolicy = permissionHandlerPolicy;
    }

    // We only create the parameters Map if required
    private Map<String,Object> parameters = null;

    @Override
    public Object getParameter(String paramName) {
        return parameters != null ? parameters.get(paramName) : null;
    }

    @Override
    public boolean isParameterSet(String paramName) {
        return parameters != null ? parameters.containsKey(paramName) : false;
    }

    @Override
    public void setParameter(String paramName, Object value) {
        if (parameters == null) {
            parameters = new HashMap<String,Object>();
        }
        parameters.put(paramName, value);
    }

    @Override
    public EventBridge getEventBridge() {
        return eventBridge;
    }

    @Override
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    @Override
    public Partition getPartition() {
        return partition;
    }

    @Override
    public PermissionHandlerPolicy getPermissionHandlerPolicy() {
        return permissionHandlerPolicy;
    }

}
