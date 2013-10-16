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

import java.util.Map;

/**
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public interface SAML2MapBasedConfig {

    /**
     * Does the config contain the key?
     *
     * @param key
     *
     * @return
     */
    boolean containsKey(String key);

    /**
     * Get a parameter from the chain config
     *
     * @param parameterName
     *
     * @return
     */
    Object getParameter(String parameterName);

    /**
     * Add a parameter
     *
     * @param parameterName
     * @param value
     */
    void addParameter(String parameterName, Object value);

    /**
     * Set the options
     *
     * @param options
     */
    void set(Map<String, Object> options);
}