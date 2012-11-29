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

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import org.picketlink.idm.query.QueryParameter;

/**
 * This interface is the base for all identity model objects.  It declares a number of
 * properties that must be supported by all identity types, in addition to defining the API
 * for identity attribute management.
 *
 * @author Shane Bryzak
 */
public interface IdentityType extends Serializable {
    /**
     *  A query parameter used to set the key value.
     */
    QueryParameter KEY = new QueryParameter() {};

    /**
     * A query parameter used to set the enabled value.
     */
    QueryParameter ENABLED = new QueryParameter() {};

    /**
     * A query parameter used to set the createdDate value
     */
    QueryParameter CREATED_DATE = new QueryParameter() {};

    /**
     * A query parameter used to set the expiryDate value
     */
    QueryParameter EXPIRY_DATE = new QueryParameter() {};

    public class AttributeParameter implements QueryParameter {
        private String name;
        public AttributeParameter(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public final class ATTRIBUTE {
        public static AttributeParameter byName(String name) {
            return new AttributeParameter(name);
        }
    }

    public class MembershipParameter implements QueryParameter {
        private String group;
        private String role;
        public MembershipParameter(String group, String role) {
            this.group = group;
            this.role = role;
        }

        public MembershipParameter group(String group) {
            this.group = group;
            return this;
        }

        public MembershipParameter role(String role) {
            this.role = role;
            return this;
        }

        public String getGroup() {
            return group;
        }

        public String getRole() {
            return role;
        }
    }

    public final class MEMBER_OF {
        public static MembershipParameter group(String name) {
            return new MembershipParameter(name, null);
        }
    }

    public final class GRANTED {
        public static MembershipParameter role(String name) {
            return new MembershipParameter(null, name);
        }
        public static MembershipParameter groupRole(String group, String role) {
            return new MembershipParameter(group, role);
        }
    }


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
     * <p>Sets the current enabled status of this {@link IdentityType}.</p>
     * 
     * @param enabled
     */
    void setEnabled(boolean enabled);
    
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
    Date getExpirationDate();
    
    /**
     * <p>Sets the date that this {@link IdentityType} expires.</p>
     * 
     * @param expirationDate
     */
    void setExpirationDate(Date expirationDate);
    
    // Attributes

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

    /**
     * Returns the owning Partition for this identity object.
     * 
     * @return
     */
    Partition getPartition();
}
