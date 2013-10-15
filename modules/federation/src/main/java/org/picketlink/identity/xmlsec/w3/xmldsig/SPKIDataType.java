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

package org.picketlink.identity.xmlsec.w3.xmldsig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Java class for SPKIDataType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SPKIDataType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence maxOccurs="unbounded">
 *         &lt;element name="SPKISexp" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class SPKIDataType {

    protected List<Object> spkiSexpAndAny = new ArrayList<Object>();

    public void addSPKI(Object obj) {
        this.spkiSexpAndAny.add(obj);
    }

    public void removeSPKI(Object obj) {
        this.spkiSexpAndAny.remove(obj);
    }

    /**
     * Gets the value of the spkiSexpAndAny property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link org.w3c.dom.Element } {@link Object }
     */
    public List<Object> getSPKISexpAndAny() {
        return Collections.unmodifiableList(this.spkiSexpAndAny);
    }
}