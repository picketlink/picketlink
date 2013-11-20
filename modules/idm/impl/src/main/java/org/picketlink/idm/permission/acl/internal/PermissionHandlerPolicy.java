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

package org.picketlink.idm.permission.acl.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.permission.acl.spi.PermissionHandler;
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
            // FIXME add back once ACL permissions are reinstated
            //registeredHandlers.add(new EntityPermissionHandler());
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

    // not supporting this feature for now - KISS
    /*
    public Object lookupResource(String identifier, Collection<Object> loadedResources) {
        Map<String,Object> loadedResourceIdentifiers = loadResourceIdentifiers(loadedResources);
        if (loadedResourceIdentifiers.containsKey(identifier)) {
            return loadedResourceIdentifiers.get(identifier);
        }
        else {
            PermissionHandler handler = getHandlerForIdentifier(identifier);
            return handler != null ? handler.lookupResource(identifier) : null;
        }
    }*/

    private Map<Serializable,Object> loadResourceIdentifiers(Collection<Object> resources) {
        if (resources == null || resources.isEmpty()) {
            return null;
        }

        Map<Serializable,Object> identifiers = new HashMap<Serializable,Object>();

        for (Object resource: resources) {
            PermissionHandler handler = getHandlerForResource(resource);

            if (handler != null) {
                Serializable identifier = handler.getIdentifier(resource);
                if (!identifiers.containsKey(identifier)) {
                    identifiers.put(identifier, resource);
                }
            }
        }

        return identifiers;
    }

    /*public Map<String,Object> lookupResources(Collection<Serializable> identifiers,
            Collection<Object> loadedResources) {
        Map<String,Object> resources = new HashMap<String,Object>();
        Map<Serializable,Object> loadedResourceIdentifiers = loadResourceIdentifiers(loadedResources);

        for (String identifier : identifiers) {
            if (loadedResourceIdentifiers.containsKey(identifier)) {
                resources.put(identifier, loadedResourceIdentifiers.get(identifier));
            } else {
                PermissionHandler handler = getHandlerForIdentifier(identifier);
                if (handler != null) {
                    Object resource = handler.lookupResource(identifier);
                    if (resource != null) {
                        resources.put(identifier, resource);
                    }
                }
           }
        }

        return resources;
    }*/

   /* private PermissionHandler getHandlerForIdentifier(String identifier) {
        for (PermissionHandler handler : handlers.values()) {
            if (handler.canLoadResource(identifier)) {
                return handler;
            }
        }

        for (PermissionHandler handler : registeredHandlers) {
            if (handler.canLoadResource(identifier)) {
                return handler;
            }
        }

        return null;
    }*/

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

    /*public Set<String> convertResourcePermissions(Object resource, Object permissions) {
        PermissionHandler handler = getHandlerForResource(resource);

        return handler != null ? handler.convertResourcePermissions(resource.getClass(), permissions) : null;
    }*/

    public Set<PermissionHandler> getRegisteredHandlers() {
        return registeredHandlers;
    }

    public void setRegisteredHandlers(Set<PermissionHandler> registeredHandlers) {
        this.registeredHandlers = registeredHandlers;
    }
}