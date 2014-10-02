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
package org.picketlink.idm.file.internal;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.permission.IdentityPermission;
import org.picketlink.idm.permission.Permission;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * @author pedroigor
 */
public class FilePermission extends AbstractFileType<Permission> {

    private static final String VERSION = "1";
    private String identityTypeId;

    protected FilePermission(IdentityType identityType, Permission permission) {
        super(VERSION, permission);
        this.identityTypeId = identityType.getId();
    }

    @Override
    protected Permission doPopulateEntry(Map<String, Serializable> properties) throws Exception {
        final Class<?> resourceClass = (Class<?>) properties.get("resourceClass");
        final String operation = (String) properties.get("operation");
        final Serializable resourceIdentifier = properties.get("resourceIdentifier");

        return new IdentityPermission(resourceClass, resourceIdentifier, new AbstractIdentityType() {
            @Override
            public String getId() {
                return identityTypeId;
            }
        }, operation);
    }

    @Override
    protected void doPopulateProperties(Map<String, Serializable> properties) throws Exception {
        Permission entry = getEntry();

        properties.put("resourceClass", entry.getResourceClass());
        properties.put("operation", entry.getOperation());
        properties.put("resourceIdentifier", entry.getResourceIdentifier());
    }

    @Override
    protected void doWriteObject(final ObjectOutputStream s) throws Exception {
        super.doWriteObject(s);
        s.writeObject(this.identityTypeId);
    }

    @Override
    protected void doReadObject(final ObjectInputStream s) throws Exception {
        super.doReadObject(s);
        this.identityTypeId = s.readObject().toString();
    }

    public String getIdentityTypeId() {
        return this.identityTypeId;
    }
}
