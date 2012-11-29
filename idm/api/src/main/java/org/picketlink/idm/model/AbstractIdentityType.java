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
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for IdentityType implementations
 * 
 * @author Shane Bryzak
 */
public abstract class AbstractIdentityType implements IdentityType {

    private static final long serialVersionUID = 1L;

    private boolean enabled = true;
    private Date createdDate = new Date();
    private Date expirationDate = null;
    private Map<String, Attribute<? extends Serializable>> attributes = 
            new HashMap<String, Attribute<? extends Serializable>>();
    private Partition partition;

    public boolean isEnabled() {
        return this.enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Date getExpirationDate() {
        return this.expirationDate;
    }
    
    @Override
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public Date getCreatedDate() {
        return this.createdDate;
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
        return java.util.Collections.unmodifiableCollection(attributes.values());
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
}
