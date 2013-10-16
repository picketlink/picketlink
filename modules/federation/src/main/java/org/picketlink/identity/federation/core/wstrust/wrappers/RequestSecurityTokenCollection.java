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

import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenCollectionType;
import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * This class represents a WS-Trust {@code RequestSecurityTokenCollection}. It wraps the JAXB representation of the
 * security
 * token collection request.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RequestSecurityTokenCollection implements BaseRequestSecurityToken {

    private final RequestSecurityTokenCollectionType delegate;

    private final List<RequestSecurityToken> requestSecurityTokens;

    /**
     * <p>
     * Creates an instance of {@code RequestSecurityTokenCollection}.
     * </p>
     */
    public RequestSecurityTokenCollection() {
        this.requestSecurityTokens = new ArrayList<RequestSecurityToken>();
        this.delegate = new RequestSecurityTokenCollectionType();
    }

    /**
     * <p>
     * Creates an instance of {@code RequestSecurityTokenCollection} using the specified delegate.
     * </p>
     *
     * @param delegate the JAXB {@code RequestSecurityTokenCollectionType} that represents a WS-Trust request
     * collection.
     */
    public RequestSecurityTokenCollection(RequestSecurityTokenCollectionType delegate) {
        this.delegate = delegate;
        this.requestSecurityTokens = new ArrayList<RequestSecurityToken>();
        for (RequestSecurityTokenType request : delegate.getRequestSecurityToken())
            this.requestSecurityTokens.add(new RequestSecurityToken(request));
    }

    /**
     * <p>
     * Obtains the collection of {@code RequestSecurityToken} objects. The returned collection is immutable, so addition
     * or
     * removal of requests must be carried by the appropriate add/remove methods.
     * </p>
     *
     * @return a {@code List<RequestSecurityToken>} containing the token requests.
     */
    public List<RequestSecurityToken> getRequestSecurityTokens() {
        return Collections.unmodifiableList(this.requestSecurityTokens);
    }

    /**
     * <p>
     * Adds the specified {@code RequestSecurityToken} object to the collection of token requests.
     * </p>
     *
     * @param request the {@code RequestSecurityToken} to be added.
     */
    public void addRequestSecurityToken(RequestSecurityToken request) {
        this.delegate.add(request.getDelegate());
        this.requestSecurityTokens.add(request);
    }

    /**
     * <p>
     * Removes the specified {@code RequestSecurityToken} object from the collection of token requests.
     * </p>
     *
     * @param request the {@code RequestSecurityToken} to be removed.
     */
    public void removeRequestSecurityToken(RequestSecurityToken request) {
        this.delegate.remove(request.getDelegate());
        this.requestSecurityTokens.remove(request);
    }

    /**
     * <p>
     * Obtains a reference to the {@code RequestSecurityTokenCollectionType} delegate.
     * </p>
     *
     * @return a reference to the delegate instance.
     */
    public RequestSecurityTokenCollectionType getDelegate() {
        return this.delegate;
    }
}
