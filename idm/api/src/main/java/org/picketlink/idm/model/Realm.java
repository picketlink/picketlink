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

package org.picketlink.idm.model;

/**
 * A Realm defines a boundary for certain identity state.  Users, Groups and standard Roles 
 * are unique within a Realm.
 * 
 * @author Shane Bryzak
 */
public class Realm implements Partition {

    private static final long serialVersionUID = -638755196631131758L;

    public static final String DEFAULT_REALM = "default";

    public static final String KEY_PREFIX = "REALM://";
    
    private String id;
    private String name;

    public Realm(String name) {
        if (name == null) {
            throw new InstantiationError("Realm name must not be null");
        }
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String getKey() {
        return String.format("%s%s", KEY_PREFIX, name);
    }

    // TODO implement hashCode() and equals() methods
}
