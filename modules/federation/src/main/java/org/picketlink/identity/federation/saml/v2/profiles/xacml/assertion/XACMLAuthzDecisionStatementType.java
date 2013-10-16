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
package org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion;

import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResponseType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;

/**
 * <p>
 * Java class for XACMLAuthzDecisionStatementType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="XACMLAuthzDecisionStatementType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:assertion}StatementAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Response"/>
 *         &lt;element ref="{urn:oasis:names:tc:xacml:2.0:context:schema:os}Request" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class XACMLAuthzDecisionStatementType extends StatementAbstractType {

    private static final long serialVersionUID = 1L;

    public static final String XSI_TYPE = "xacml-saml:XACMLAuthzDecisionStatementType";

    protected ResponseType response;

    protected RequestType request;

    /**
     * Gets the value of the response property.
     *
     * @return possible object is {@link ResponseType }
     */
    public ResponseType getResponse() {
        return response;
    }

    /**
     * Sets the value of the response property.
     *
     * @param value allowed object is {@link ResponseType }
     */
    public void setResponse(ResponseType value) {
        this.response = value;
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
}