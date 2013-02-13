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
package org.picketlink.identity.xmlsec.w3.xmlenc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.identity.xmlsec.w3.xmldsig.TransformType;

/**
 * <p>
 * Java class for TransformsType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TransformsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.w3.org/2000/09/xmldsig#}Transform" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class TransformsType {
    protected List<TransformType> transform = new ArrayList<TransformType>();

    public void add(TransformType tt) {
        this.transform.add(tt);
    }

    public void addAll(List<TransformType> ttlist) {
        this.transform.addAll(ttlist);
    }

    public void remove(TransformType tt) {
        this.transform.remove(tt);
    }

    /**
     * Gets the value of the transform property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link TransformType }
     */
    public List<TransformType> getTransform() {
        return Collections.unmodifiableList(this.transform);
    }
}