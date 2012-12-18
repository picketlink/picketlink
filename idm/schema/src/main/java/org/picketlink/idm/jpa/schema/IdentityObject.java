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

package org.picketlink.idm.jpa.schema;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.picketlink.idm.jpa.annotations.EntityType;
import org.picketlink.idm.jpa.annotations.IDMAttribute;
import org.picketlink.idm.jpa.annotations.IDMEntity;
import org.picketlink.idm.jpa.annotations.IDMProperty;
import org.picketlink.idm.jpa.annotations.PropertyType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@IDMEntity(EntityType.IDENTITY_TYPE)
@Entity
public class IdentityObject {

    @Id
    @GeneratedValue
    private long internalId;

    @IDMProperty(PropertyType.DISCRIMINATOR)
    private String discriminator;

    @IDMProperty(PropertyType.ID)
    private String id;

    @IDMProperty(PropertyType.KEY)
    private String key;

    @IDMProperty(PropertyType.NAME)
    private String name;

    @IDMProperty(PropertyType.FIRST_NAME)
    private String firstName;

    @IDMProperty(PropertyType.LAST_NAME)
    private String lastName;

    @IDMProperty(PropertyType.EMAIL)
    private String email;

    @IDMProperty(PropertyType.ENABLED)
    private boolean enabled;

    @IDMProperty(PropertyType.CREATION_DATE)
    private Date creationDate;

    @IDMProperty(PropertyType.EXPIRY_DATE)
    private Date expiryDate;

    @IDMAttribute(name = "one-valued")
    private String oneValued;

    @ManyToOne
    @IDMProperty(PropertyType.PARENT_GROUP)
    private IdentityObject parent;

    public long getInternalId() {
        return this.internalId;
    }

    public void setInternalId(long internalId) {
        this.internalId = internalId;
    }

    public String getDiscriminator() {
        return this.discriminator;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return getDiscriminator() + "://" + getId();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public IdentityObject getParent() {
        return this.parent;
    }

    public void setParent(IdentityObject parent) {
        this.parent = parent;
    }

    public String getOneValued() {
        return this.oneValued;
    }

    public void setOneValued(String oneValued) {
        this.oneValued = oneValued;
    }
}
