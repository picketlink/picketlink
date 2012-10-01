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

import java.util.HashMap;
import java.util.Map;

import org.picketlink.idm.query.Range;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 *
 */
public class AbstractQuery<T> {

    private String name;
    private Map<String, String[]> attributeFilters = new HashMap<String, String[]>();
    private boolean enabled = true;
    private boolean sortAscending;

    public T reset() {
        return (T) this;
    }

    public T getImmutable() {
        return (T) this;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public String getName() {
        return this.name;
    }

    public T setAttributeFilter(String name, String[] values) {
        this.attributeFilters.put(name, values);
        return (T) this;
    }

    public T setEnabled(boolean enabled) {
        this.enabled = enabled;
        return (T) this;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public Map<String, String[]> getAttributeFilters() {
        return this.attributeFilters;
    }

    public void setRange(Range range) {

    }

    public Range getRange() {
        return null;
    }

    public T sort(boolean ascending) {
        this.sortAscending = ascending;
        return (T) this;
    }

}
