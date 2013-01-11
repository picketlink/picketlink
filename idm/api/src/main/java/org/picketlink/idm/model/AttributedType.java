package org.picketlink.idm.model;

import java.io.Serializable;
import java.util.Collection;

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
    QueryParameter ID = new QueryParameter() {};

    /**
     * Returns the unique identifier for this instance
     * @return
     */
    String getId();

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
}
