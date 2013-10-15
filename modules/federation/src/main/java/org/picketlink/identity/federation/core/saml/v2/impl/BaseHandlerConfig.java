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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class BaseHandlerConfig {

    protected Map<String, Object> params = new HashMap<String, Object>();

    /**
     * @see DefaultSAML2HandlerChainConfig#containsKey(String)
     */
    public boolean containsKey(String key) {
        return params.containsKey(key);
    }

    /**
     * @see DefaultSAML2HandlerChainConfig#getParameter(String)
     */
    public Object getParameter(String parameterName) {
        return params.get(parameterName);
    }

    /**
     * @see org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2MapBasedConfig#addParameter(java.lang.String,
     *      java.lang.Object)
     */
    public void addParameter(String parameterName, Object value) {
        this.params.put(parameterName, value);
    }

    public void set(Map<String, Object> options) {
        this.params.putAll(options);
    }
}