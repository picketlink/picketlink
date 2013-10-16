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
package org.picketlink.identity.federation.core.saml.v2.interfaces;

import java.util.Collection;
import java.util.Set;

/**
 * A SAML2 chain of handlers
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 1, 2009
 */
public interface SAML2HandlerChain {

    /**
     * Number of handlers
     *
     * @return
     */
    int size();

    /**
     * Get a read-only set of handlers
     *
     * @return
     */
    Set<SAML2Handler> handlers();

    /**
     * Add an handler
     *
     * @param handler
     *
     * @return whether add was successful
     */
    boolean add(SAML2Handler handler);

    /**
     * Add a collection of handlers
     *
     * @param handlers
     *
     * @return
     */
    boolean addAll(Collection<SAML2Handler> handlers);

    /**
     * Remove an handler
     *
     * @param handler
     *
     * @return whether remove was successful
     */
    boolean remove(SAML2Handler handler);

    /**
     * Remove a collection of handlers
     *
     * @param handlers
     *
     * @return
     */
    boolean removeAll(Collection<SAML2Handler> handlers);
}