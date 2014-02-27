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

import org.jboss.security.xacml.core.model.context.RequestType;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for XACMLAuthzDecisionQueryType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="XACMLAuthzDecisionQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}RequestAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Request"/>
 *       &lt;/sequence>
 *       &lt;attribute name="InputContextOnly" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="ReturnContext" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class XACMLAuthzDecisionQueryType extends RequestAbstractType {

    private static final long serialVersionUID = 1L;

    protected RequestType request;

    protected Boolean inputContextOnly = Boolean.FALSE;

    protected Boolean returnContext = Boolean.FALSE;

    public XACMLAuthzDecisionQueryType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    /**
     * Gets the value of the request property.
     *
     * @return possible object is {@link RequestType }
     */
    public RequestType getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     *
     * @param value allowed object is {@link RequestType }
     */
    public void setRequest(RequestType value) {
        this.request = value;
    }

    /**
     * Gets the value of the inputContextOnly property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isInputContextOnly() {
        if (inputContextOnly == null) {
            return false;
        } else {
            return inputContextOnly;
        }
    }

    /**
     * Sets the value of the inputContextOnly property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setInputContextOnly(Boolean value) {
        this.inputContextOnly = value;
    }

    /**
     * Gets the value of the returnContext property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isReturnContext() {
        if (returnContext == null) {
            return false;
        } else {
            return returnContext;
        }
    }

    /**
     * Sets the value of the returnContext property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setReturnContext(Boolean value) {
        this.returnContext = value;
    }

}