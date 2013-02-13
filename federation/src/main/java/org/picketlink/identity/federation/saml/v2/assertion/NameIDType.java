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
package org.picketlink.identity.federation.saml.v2.assertion;

import java.net.URI;

/**
 * Represents a NameIDType
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 24, 2010
 */
public class NameIDType extends BaseIDAbstractType {
    /*
     * <complexType name="NameIDType"> <simpleContent> <extension base="string"> <attributeGroup ref="saml:IDNameQualifiers"/>
     * <attribute name="Format" type="anyURI" use="optional"/> <attribute name="SPProvidedID" type="string" use="optional"/>
     * </extension> </simpleContent> </complexType>
     *
     * <attributeGroup name="IDNameQualifiers"> <attribute name="NameQualifier" type="string" use="optional"/> <attribute
     * name="SPNameQualifier" type="string" use="optional"/> </attributeGroup>
     */

    private static final long serialVersionUID = 1L;
    private String value;
    private URI format;
    private String sPProvidedID;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getsPProvidedID() {
        return sPProvidedID;
    }

    public void setsPProvidedID(String sPProvidedID) {
        this.sPProvidedID = sPProvidedID;
    }

    public URI getFormat() {
        return format;
    }

    public void setFormat(URI format) {
        this.format = format;
    }

    public String getSPProvidedID() {
        return sPProvidedID;
    }

    public void setSPProvidedID(String sPProvidedID) {
        this.sPProvidedID = sPProvidedID;
    }
}