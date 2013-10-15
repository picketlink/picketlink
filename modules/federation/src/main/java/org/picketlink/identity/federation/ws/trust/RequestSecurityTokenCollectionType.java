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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The RequestSecurityTokenCollection (RSTC) element is used to provide multiple RST requests. One or more RSTR
 * elements
 * in an
 * RSTRC element are returned in the response to the RequestSecurityTokenCollection.
 *
 *
 * <p>
 * Java class for RequestSecurityTokenCollectionType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="RequestSecurityTokenCollectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RequestSecurityToken" type="{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}RequestSecurityTokenType"
 * maxOccurs="unbounded" minOccurs="2"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class RequestSecurityTokenCollectionType implements SimpleCollectionUsage<RequestSecurityTokenType> {

    protected List<RequestSecurityTokenType> requestSecurityToken = new ArrayList<RequestSecurityTokenType>();

    /**
     * Gets the value of the requestSecurityToken property.
     * <p>
     * Objects of the following type(s) are allowed in the list {@link RequestSecurityTokenType }
     *
     *
     */
    public List<RequestSecurityTokenType> getRequestSecurityToken() {
        return Collections.unmodifiableList(this.requestSecurityToken);
    }

    public void add(RequestSecurityTokenType t) {
        this.requestSecurityToken.add(t);
    }

    public boolean remove(RequestSecurityTokenType t) {
        return requestSecurityToken.remove(t);
    }
}