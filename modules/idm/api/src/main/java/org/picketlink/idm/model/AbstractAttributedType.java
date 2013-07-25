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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;

/**
 * Abstract base class for all AttributedType implementations
 *
 * @author Shane Bryzak
 *
 */
public abstract class AbstractAttributedType implements AttributedType {
    private static final long serialVersionUID = -6118293036241099199L;

    private String id;

    private Map<String, Attribute<? extends Serializable>> attributes =
            new HashMap<String, Attribute<? extends Serializable>>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAttribute(Attribute<? extends Serializable> attribute) {
        attributes.put(attribute.getName(), attribute);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @SuppressWarnings("unchecked")
    public <T extends Serializable> Attribute<T> getAttribute(String name) {
        return (Attribute<T>) attributes.get(name);
    }

    public Collection<Attribute<? extends Serializable>> getAttributes() {
        return unmodifiableCollection(attributes.values());
    }

    public Map<String, Attribute<? extends Serializable>> getAttributesMap() {
        return unmodifiableMap(attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!getClass().isInstance(obj)) {
            return false;
        }

        AttributedType other = (AttributedType) obj;

        return getId() != null && other.getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }

}
