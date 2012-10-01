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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.picketlink.idm.model.User;

/**
 * <p>
 * JPA Entity that maps {@link User} instances.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Entity
@NamedQuery(name = NamedQueries.USER_LOAD_BY_KEY, query = "from DatabaseUser where key = :key")
public class DatabaseUser extends AbstractDatabaseIdentityType<DatabaseUserAttribute> implements User {

    private String firstName;
    private String lastName;

    private String email;

    private String fullName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DatabaseUserAttribute> ownerAttributes = new ArrayList<DatabaseUserAttribute>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DatabaseMembership> memberships = new ArrayList<DatabaseMembership>();

    public DatabaseUser() {
    }

    public DatabaseUser(String key) {
        super(key);
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param laststring_idName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the fullName
     */
    @Transient
    public String getFullName() {
        if (this.fullName == null) {
            this.fullName = this.getFirstName() + " " + this.getLastName();
        }

        return this.fullName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the memberships
     */
    public List<DatabaseMembership> getMemberships() {
        return memberships;
    }

    /**
     * @param memberships the memberships to set
     */
    public void setMemberships(List<DatabaseMembership> memberships) {
        this.memberships = memberships;
    }

    /**
     * @return the userAttributes
     */
    @Override
    public List<DatabaseUserAttribute> getOwnerAttributes() {
        return ownerAttributes;
    }

    /**
     * @param userAttributes the userAttributes to set
     */
    public void setOwnerAttributes(List<DatabaseUserAttribute> userAttributes) {
        this.ownerAttributes = userAttributes;
    }

    @Override
    protected DatabaseUserAttribute createAttribute(String name, String value) {
        return new DatabaseUserAttribute(name, value);
    }

    // TODO: implement hashcode and equals methods
}