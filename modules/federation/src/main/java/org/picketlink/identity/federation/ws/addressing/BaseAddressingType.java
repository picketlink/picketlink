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
package org.picketlink.identity.federation.ws.addressing;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anil.Saldhana@redhat.com
 * @since May 17, 2011
 */
public class BaseAddressingType {

    protected final Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Add an other attribute
     *
     * @param qname
     * @param str
     */
    public void addOtherAttribute(QName qname, String str) {
        otherAttributes.put(qname, str);
    }

    public void addOtherAttributes(Map<QName, String> otherMap) {
        otherAttributes.putAll(otherMap);
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     *
     * <p>
     * the map is keyed by the name of the attribute and the value is the string value of the attribute.
     *
     * the map returned by this method is live, and you can add new attribute by updating the map directly. Because of
     * this
     * design, there's no setter.
     *
     * @return always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return Collections.unmodifiableMap(otherAttributes);
    }
}