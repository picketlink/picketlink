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
package org.picketlink.identity.federation.core.wstrust.wrappers;

import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenResponseCollectionType;
import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenResponseType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * This class represents a WS-Trust {@code RequestSecurityTokenResponseCollection}. It wraps the JAXB representation of
 * the
 * security token collection response.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RequestSecurityTokenResponseCollection implements BaseRequestSecurityTokenResponse {

    private final RequestSecurityTokenResponseCollectionType delegate;

    private final List<RequestSecurityTokenResponse> requestSecurityTokenResponses;

    /**
     * <p>
     * Creates an instance of {@code RequestSecurityTokenResponseCollection}.
     * </p>
     */
    public RequestSecurityTokenResponseCollection() {
        this.requestSecurityTokenResponses = new ArrayList<RequestSecurityTokenResponse>();
        this.delegate = new RequestSecurityTokenResponseCollectionType();
    }

    /**
     * <p>
     * Creates an instance of {@code RequestSecurityTokenResponseCollection} using the specified delegate.
     * </p>
     *
     * @param delegate the JAXB {@code RequestSecurityTokenResponseCollectionType} that represents a WS-Trust request
     * collection.
     */
    public RequestSecurityTokenResponseCollection(RequestSecurityTokenResponseCollectionType delegate) {
        this.delegate = delegate;
        this.requestSecurityTokenResponses = new ArrayList<RequestSecurityTokenResponse>();
        for (RequestSecurityTokenResponseType response : delegate.getRequestSecurityTokenResponse())
            this.requestSecurityTokenResponses.add(new RequestSecurityTokenResponse(response));
    }

    /**
     * <p>
     * Obtains the collection of {@code RequestSecurityTokenResponse} objects. The returned collection is immutable, so
     * addition
     * or removal of requests must be carried by the appropriate add/remove methods.
     * </p>
     *
     * @return a {@code List<RequestSecurityToken>} containing the token requests.
     */
    public List<RequestSecurityTokenResponse> getRequestSecurityTokenResponses() {
        return Collections.unmodifiableList(this.requestSecurityTokenResponses);
    }

    /**
     * <p>
     * Adds the specified {@code RequestSecurityTokenResponse} object to the collection of token requests.
     * </p>
     *
     * @param request the {@code RequestSecurityTokenResponse} to be added.
     */
    public void addRequestSecurityTokenResponse(RequestSecurityTokenResponse response) {
        this.delegate.add(response.getDelegate());
        this.requestSecurityTokenResponses.add(response);
    }

    /**
     * <p>
     * Removes the specified {@code RequestSecurityTokenResponse} object from the collection of token requests.
     * </p>
     *
     * @param request the {@code RequestSecurityTokenResponse} to be removed.
     */
    public void removeRequestSecurityTokenResponse(RequestSecurityTokenResponse response) {
        this.delegate.remove(response.getDelegate());
        this.requestSecurityTokenResponses.remove(response);
    }

    /**
     * <p>
     * Obtains a reference to the {@code RequestSecurityTokenResponseCollectionType} delegate.
     * </p>
     *
     * @return a reference to the delegate instance.
     */
    public RequestSecurityTokenResponseCollectionType getDelegate() {
        return this.delegate;
    }

}
