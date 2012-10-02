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

import org.picketlink.idm.model.Role;

/**
 * <p>
 * JPA Entity that maps {@link Role} instances.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
@Entity
@NamedQuery(name = NamedQueries.ROLE_LOAD_BY_KEY, query = "from DatabaseRole where key = :key")
public class DatabaseRole extends AbstractDatabaseIdentityType<DatabaseRoleAttribute> implements Role {

    private String name;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<DatabaseRoleAttribute> ownerAttributes = new ArrayList<DatabaseRoleAttribute>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL)
    private List<DatabaseMembership> memberships = new ArrayList<DatabaseMembership>();

    public DatabaseRole() {
    }

    public DatabaseRole(String name) {
        super(name);
        setName(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<DatabaseRoleAttribute> getOwnerAttributes() {
        return this.ownerAttributes;
    }

    public void setOwnerAttributes(List<DatabaseRoleAttribute> ownerAttributes) {
        this.ownerAttributes = ownerAttributes;
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

    @Override
    protected DatabaseRoleAttribute createAttribute(String name, String value) {
        return new DatabaseRoleAttribute(name, value);
    }

    // TODO: implement hashcode and equals methods
}
