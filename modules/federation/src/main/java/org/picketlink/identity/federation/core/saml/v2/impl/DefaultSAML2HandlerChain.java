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
package org.picketlink.identity.federation.core.saml.v2.impl;

import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Default implementation of the SAML2 handler chain
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 1, 2009
 */
public class DefaultSAML2HandlerChain implements SAML2HandlerChain {

    private Set<SAML2Handler> handlers = new LinkedHashSet<SAML2Handler>();

    /**
     * @see SAML2HandlerChain#add(SAML2Handler)
     */
    public boolean add(SAML2Handler handler) {
        return handlers.add(handler);
    }

    /**
     * @see SAML2HandlerChain#add(SAML2Handler)
     */
    public boolean addAll(Collection<SAML2Handler> handlers) {
        return this.handlers.addAll(handlers);
    }

    /**
     * @see SAML2HandlerChain#handlers()
     */
    public Set<SAML2Handler> handlers() {
        return Collections.unmodifiableSet(handlers);
    }

    /**
     * @see SAML2HandlerChain#remove(SAML2Handler)
     */
    public boolean remove(SAML2Handler handler) {
        return handlers.remove(handler);
    }

    /**
     * @see SAML2HandlerChain#size()
     */
    public int size() {
        return handlers.size();
    }

    /**
     * @see SAML2HandlerChain#removeAll(Collection)
     */
    public boolean removeAll(Collection<SAML2Handler> handlers) {
        return handlers.removeAll(handlers);
    }
}