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
package org.picketlink.idm.ldap.internal;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Attributes of an {@link LDAPUser} that does not map to LDAP managed attributes
 * 
 * @author anil saldhana
 * @since Sep 7, 2012
 */
public class LDAPCustomAttributes implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Serializable> attributes = new HashMap<String, Serializable>();

    public void addAttribute(String key, Serializable value) {
        attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    public Map<String, Serializable> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void clear() {
        Set<Entry<String, Object>> entrySet = new HashMap(this.attributes).entrySet();
        
        for (Entry<String, Object> entry : entrySet) {
            if (!entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED)
                    && !entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE)) {
                this.attributes.remove(entry.getKey());
            }
        }
    }

}