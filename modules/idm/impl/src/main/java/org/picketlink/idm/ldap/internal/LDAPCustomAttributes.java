/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.idm.ldap.internal;

import org.picketlink.common.constants.LDAPConstants;

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

    private static final long serialVersionUID = 3682970889889505951L;

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

    public Serializable getAttribute(String name) {
        return attributes.get(name);
    }

    public void clear() {
        Set<Entry<String, Object>> entrySet = new HashMap<String, Object>(this.attributes).entrySet();

        for (Entry<String, Object> entry : entrySet) {
            if (!entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_ENABLED)
                    && !entry.getKey().equals(LDAPConstants.CUSTOM_ATTRIBUTE_EXPIRY_DATE)) {
                this.attributes.remove(entry.getKey());
            }
        }
    }

}