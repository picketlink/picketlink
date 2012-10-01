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
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for IdentityType implementations
 */
public abstract class AbstractIdentityType implements IdentityType {
    private boolean enabled = true;
    private Date creationDate = null;
    private Date expirationDate = null;
    private Map<String, String[]> attributes = new HashMap<String, String[]>();

    public boolean isEnabled() {
        return this.enabled;
    }

    public Date getExpirationDate() {
        return this.expirationDate;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, new String[] { value });
    }

    public void setAttribute(String name, String[] values) {
        attributes.put(name, values);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public String getAttribute(String name) {
        String[] vals = attributes.get(name);
        return null == vals ? null : ((vals.length != 0) ? vals[0] : null);
    }

    public String[] getAttributeValues(String name) {
        return attributes.get(name);
    }

    public Map<String, String[]> getAttributes() {
        return java.util.Collections.unmodifiableMap(attributes);
    }

}
