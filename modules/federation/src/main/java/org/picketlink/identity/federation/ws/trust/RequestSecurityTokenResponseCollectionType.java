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
package org.picketlink.identity.federation.ws.trust;

import org.picketlink.identity.federation.ws.addressing.AnyAddressingType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The <wst:RequestSecurityTokenResponseCollection> element (RSTRC) MUST be used to return a security token or response
 * to a
 * security token request on the final response.
 *
 *
 * <p>
 * Java class for RequestSecurityTokenResponseCollectionType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RequestSecurityTokenResponseCollectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}RequestSecurityTokenResponse"
 * maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class RequestSecurityTokenResponseCollectionType extends AnyAddressingType implements
        SimpleCollectionUsage<RequestSecurityTokenResponseType> {

    protected List<RequestSecurityTokenResponseType> requestSecurityTokenResponse = new ArrayList<RequestSecurityTokenResponseType>();

    /**
     * Gets the value of the requestSecurityTokenResponse property.
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link RequestSecurityTokenResponseType }
     */
    public List<RequestSecurityTokenResponseType> getRequestSecurityTokenResponse() {
        return Collections.unmodifiableList(this.requestSecurityTokenResponse);
    }

    public void add(RequestSecurityTokenResponseType t) {
        this.requestSecurityTokenResponse.add(t);

    }

    public boolean remove(RequestSecurityTokenResponseType t) {
        return this.requestSecurityTokenResponse.remove(t);
    }
}