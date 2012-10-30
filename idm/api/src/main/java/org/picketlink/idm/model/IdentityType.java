/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.idm.model;

import java.util.Date;
import java.util.Map;

import org.picketlink.idm.query.QueryParameter;

/**
 * This interface is the base for all identity model objects.  It declares a number of
 * properties that must be supported by all identity types, in addition to defining the API
 * for identity attribute management.
 *
 * @author Shane Bryzak
 */
public interface IdentityType {
    /**
     *  A query parameter used to set the key value.
     */
    QueryParameter PARAM_KEY = new QueryParameter() {};

    /**
     * A query parameter used to set the enabled value.
     */
    QueryParameter PARAM_ENABLED = new QueryParameter() {};
    
    /**
     * A query parameter used to set the createdDate value
     */
    QueryParameter PARAM_CREATED_DATE = new QueryParameter() {};
    
    /**
     * A query parameter used to set the expiryDate value
     */
    QueryParameter PARAM_EXPIRY_DATE = new QueryParameter() {};


    /**
     * Returns a key value for this IdentityType.  The key may be used to perform a 
     * lookup operation to retrieve the exact IdentityType instance, and so must be unique.
     *  
     * @return A String value representing the unique key value
     */
    String getKey();

    /**
     * Indicates the current enabled status of this IdentityType.
     * 
     * @return A boolean value indicating whether this IdentityType is enabled.
     */
    boolean isEnabled();


    /**
     * Returns the date that this IdentityType instance was created.
     * 
     * @return Date value representing the creation date
     */
    Date getCreatedDate();

    /**
     * Returns the date that this IdentityType expires, or null if there is no expiry date.
     * 
     * @return
     */
    Date getExpiryDate();

    // Attributes

    /**
     * Set the attribute with given name and value. This operation will overwrite any previous value. 
     * A null value will remove the attribute.
     *
     * @param name of attribute
     * @param value to be set
     */
    void setAttribute(String name, String value);

    /**
     * Set the attribute with given name and values. This operation will overwrite any previous values. 
     * A null value or empty array will remove the attribute.
     *
     * @param name of attribute
     * @param values to be set
     */
    void setAttribute(String name, String[] values);

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
    String getAttribute(String name);

    /**
     * Return the attribute values with the specified name
     * 
     * @param name of attribute
     * @return attribute values or null if attribute with given name doesn't exist
     */
    String[] getAttributeValues(String name);

    /**
     * Returns a Map containing all attribute values for this IdentityType instance.
     * 
     * @return map of attribute names and their values
     */
    Map<String, String[]> getAttributes();

}
