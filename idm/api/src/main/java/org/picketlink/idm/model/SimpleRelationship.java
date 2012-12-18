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

/**
 * An instance of {@link Relationship}
 * @author anil saldhana
 * @since Dec 18, 2012
 */
public class SimpleRelationship extends AbstractIdentityType implements Relationship {
    private static final long serialVersionUID = 1L;

    private String id = null;

    private String name;

    private IdentityType to;

    private IdentityType from;
    
    public SimpleRelationship(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating SimpleRelationship - name cannot be null or empty");
        }

        this.name = name;
    }
    @Override
    public String getKey() { 
        return String.format("%s%s", KEY_PREFIX, getId());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public SimpleRelationship setId(String id) {
        this.id = id;
        return this;
    }

    public SimpleRelationship setName(String name) {
        this.name = name;
        return this;
    }
    @Override
    public IdentityType from() {
        return this.from;
    }
    @Override
    public IdentityType to() {
        return this.to;
    }
    public SimpleRelationship setTo(IdentityType to) {
        this.to = to;
        return this;
    }
    public SimpleRelationship setFrom(IdentityType from) {
        this.from = from;
        return this;
    }
}