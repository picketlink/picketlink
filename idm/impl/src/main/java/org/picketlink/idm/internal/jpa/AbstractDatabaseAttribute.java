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

package org.picketlink.idm.internal.jpa;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.picketlink.idm.model.IdentityType;

/**
 * <p>
 * Base class for JPA Entities that stores name/value pairs. Subclasses should override the abstract methods to provide
 * additional information about the entity that owns the attribute.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@MappedSuperclass
public abstract class AbstractDatabaseAttribute<OWNER extends IdentityType> {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @Lob
    private String value;

    public AbstractDatabaseAttribute() {
    }

    public AbstractDatabaseAttribute(String name, String value) {
        setName(name);
        setValue(value);
    }

    /**
     * @return the id
     */
    public String getId() {
        return String.valueOf(id);
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = Long.valueOf(id);
    }

    /**
     * <p>
     * Subclasses must override this method to return the owner of this attribute.
     * </p>
     *
     * @return
     */
    protected abstract OWNER getIdentityType();

    /**
     * <p>
     * Sets the owner of this attribute.
     * </p>
     *
     * @param identityType
     */
    protected abstract void setIdentityType(OWNER identityType);

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AbstractDatabaseAttribute)) {
            return false;
        }

        AbstractDatabaseAttribute other = (AbstractDatabaseAttribute) obj;

        return new EqualsBuilder().append(getId(), other.getId()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId()).append("owner", getIdentityType()).append("name", getName())
                .append("value", getValue()).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(getId()).append(getName()).append(getValue()).toHashCode();
    }

}