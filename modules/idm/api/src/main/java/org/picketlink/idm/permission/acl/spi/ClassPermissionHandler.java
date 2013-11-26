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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.picketlink.idm.permission.annotations.PermissionsHandledBy;

/**
 * An Identifier strategy for class-based permission checks
 *
 * @author Shane Bryzak
 */
public class ClassPermissionHandler extends BaseAbstractPermissionHandler implements PermissionHandler {
    private Map<Class<?>, String> identifierNames = new ConcurrentHashMap<Class<?>, String>();

    @Override
    public boolean canHandle(Class<?> resourceClass) {
        return Class.class.equals(resourceClass);
    }

    @Override
    public Serializable getIdentifier(Object resource) {
        if (!(resource instanceof Class<?>)) {
            throw new IllegalArgumentException("Resource [" + resource + "] must be instance of Class");
        }

        return getIdentifierName((Class<?>) resource);
    }

    private String getIdentifierName(Class<?> cls) {
        if (!identifierNames.containsKey(cls)) {
            String name = null;

            if (cls.isAnnotationPresent(PermissionsHandledBy.class)) {
                PermissionsHandledBy handledBy = (PermissionsHandledBy) cls.getAnnotation(PermissionsHandledBy.class);
                if (handledBy.name() != null && !"".equals(handledBy.name().trim()))
                {
                    name = handledBy.name();
                }
            }

            if (name == null) {
                name = cls.getName().substring(cls.getName().lastIndexOf('.') + 1);
            }

            identifierNames.put(cls, name);
            return name;
        }

        return identifierNames.get(cls);
    }

    @Override
    public Class<?> unwrapResourceClass(Object resource) {
        return Class.class;
    }
}
