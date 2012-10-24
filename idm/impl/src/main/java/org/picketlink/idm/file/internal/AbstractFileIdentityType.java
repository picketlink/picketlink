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

package org.picketlink.idm.file.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.model.IdentityType;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public abstract class AbstractFileIdentityType implements IdentityType, Serializable {

    private String key;
    private boolean enabled = true;
    private Date creationDate = null;
    private Date expirationDate = null;
    private Map<String, String[]> attributes = new HashMap<String, String[]>();

    protected transient FileChangeListener changeListener;

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        update();
    }

    /**
     * @return the expirationDate
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * @param expirationDate the expirationDate to set
     */
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
        update();
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        update();
    }

    protected abstract void update();

    @Override
    public void setAttribute(String name, String value) {
        this.attributes.put(name, new String[] { value });
        update();
    }

    @Override
    public void setAttribute(String name, String[] values) {
        this.attributes.put(name, values);
        update();
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
        update();
    }

    @Override
    public String getAttribute(String name) {
        String[] attribute = this.attributes.get(name);

        if (attribute == null || attribute.length == 0) {
            return null;
        }

        return attribute[0];
    }

    @Override
    public String[] getAttributeValues(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Map<String, String[]> getAttributes() {
        return this.attributes;
    }
    
    void setChangeListener(FileChangeListener changeListener) {
        this.changeListener = changeListener;
    }
}
