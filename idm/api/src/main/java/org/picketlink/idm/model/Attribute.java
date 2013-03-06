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

import java.io.Serializable;

/**
 * Represents an attribute value, a type of metadata that can be associated with an IdentityType
 *
 * @author Shane Bryzak
 *
 * @param <T>
 */
public class Attribute<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = 237211288303510728L;

    /**
     * The name of the attribute
     */
    private String name;

    /**
     * The attribute value.
     */
    private T value;

    /**
     * Indicates whether this Attribute has a read-only value
     */
    private boolean readOnly = false;

    /**
     * Indicates whether the Attribute value has been loaded
     */
    private boolean loaded = false;

    public Attribute(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public Attribute(String name, T value, boolean readOnly) {
        this(name, value);
        this.readOnly = readOnly;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean value) {
        this.loaded = value;
    }

    /**
     * Sets the value for this attribute.  If the Attribute value is readOnly, a RuntimeException is thrown.
     *
     * @param value
     */
    public void setValue(T value) {
        if (readOnly) {
            throw new RuntimeException("Error setting Attribute value [" + name + " ] - value is read only.");
        }
        this.value = value;
    }
}


