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
import org.picketlink.idm.query.AttributeParameter;
import org.picketlink.idm.query.QueryParameter;

/**
 *
 * @author Shane Bryzak
 *
 */
public interface AttributedType extends Serializable {

    /**
     * A query parameter used to set the id value.
     */
    QueryParameter ID = new AttributeParameter("id");

    /**
     * Returns the unique identifier for this instance
     * @return
     */
    String getId();

    /**
     * Sets the unique identifier for this instance
     * @return
     */
    void setId(String id);

    /**
     * Set the specified attribute. This operation will overwrite any previous value.
     *
     * @param name of attribute
     * @param value to be set
     */
    void setAttribute(Attribute<? extends Serializable> attribute);

    /**
     * Remove the attribute with given name
     *
     * @param name of attribute
     */
    void removeAttribute(String name);

    /**
     * Return the attribute value with the specified name
     *
     * @param name of attribute
     * @return attribute value or null if attribute with given name doesn't exist. If given attribute has many values method
     *         will return first one
     */
    <T extends Serializable> Attribute<T> getAttribute(String name);

    /**
     * Returns a Map containing all attribute values for this IdentityType instance.
     *
     * @return map of attribute names and their values
     */
    Collection<Attribute<? extends Serializable>> getAttributes();

    public final class QUERY_ATTRIBUTE {
        public static AttributeParameter byName(String name) {
            return new AttributeParameter(name);
        }
    }
}
