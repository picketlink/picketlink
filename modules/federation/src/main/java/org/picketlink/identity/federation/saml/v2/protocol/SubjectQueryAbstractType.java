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
package org.picketlink.identity.federation.saml.v2.protocol;

import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for SubjectQueryAbstractType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SubjectQueryAbstractType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Subject"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public abstract class SubjectQueryAbstractType extends RequestAbstractType {

    private static final long serialVersionUID = 1L;

    protected SubjectType subject;

    public SubjectQueryAbstractType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    public void setSubject(SubjectType subject) {
        this.subject = subject;
    }

    /**
     * Gets the value of the subject property.
     *
     * @return possible object is {@link SubjectType }
     */
    public SubjectType getSubject() {
        return subject;
    }
}