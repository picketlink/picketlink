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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.permission.annotations.PermissionsHandledBy;

/**
 * Manages a set of PermissionHandler instances that overall define a "policy" for
 * how persistent resource permissions are mapped and managed.
 *
 * @author Shane Bryzak
 */
public class PermissionHandlerPolicy {
    private Map<Class<?>, PermissionHandler> handlers = new ConcurrentHashMap<Class<?>, PermissionHandler>();

    private Set<PermissionHandler> registeredHandlers = new HashSet<PermissionHandler>();

    public PermissionHandlerPolicy(Set<PermissionHandler> registeredHandlers) {
        if (registeredHandlers.isEmpty()) {
            registeredHandlers.add(new EntityPermissionHandler());
            registeredHandlers.add(new ClassPermissionHandler());
        }
    }

    public Serializable getIdentifier(Object resource) {
        if (resource instanceof String) {
            return (String) resource;
        }

        PermissionHandler handler = getHandlerForResource(resource);

        return handler != null ? handler.getIdentifier(resource) : null;
    }

    public Class<?> getResourceClass(Object resource) {
        if (resource instanceof String) {
            return String.class;
        }

        PermissionHandler handler = getHandlerForResource(resource);

        return handler != null ? handler.unwrapResourceClass(resource) : null;
    }

    private PermissionHandler getHandlerForResource(Object resource) {
        PermissionHandler handler = handlers.get(resource.getClass());

        if (handler == null) {
            if (resource.getClass().isAnnotationPresent(PermissionsHandledBy.class)) {
                Class<? extends PermissionHandler> handlerClass =
                        resource.getClass().getAnnotation(PermissionsHandledBy.class).value();

                if (handlerClass != PermissionHandler.class) {
                    try {
                        handler = handlerClass.newInstance();
                        handlers.put(resource.getClass(), handler);
                    }
                    catch (Exception ex) {
                        throw new RuntimeException("Error instantiating IdentifierStrategy for object " + resource, ex);
                    }
                }
            }

            for (PermissionHandler s : registeredHandlers) {
                if (s.canHandle(resource.getClass())) {
                    handler = s;
                    handlers.put(resource.getClass(), handler);
                    break;
                }
            }
        }

        return handler;
    }

    public Set<PermissionHandler> getRegisteredHandlers() {
        return registeredHandlers;
    }

    public void setRegisteredHandlers(Set<PermissionHandler> registeredHandlers) {
        this.registeredHandlers = registeredHandlers;
    }
}