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
package org.picketlink.identity.federation.core.impl;

import org.picketlink.identity.federation.core.interfaces.AttributeManager;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An attribute manager implementation that just returns an empty attribute map
 *
 * @author Anil.Saldhana@redhat.com
 * @since Sep 10, 2009
 */
public class EmptyAttributeManager implements AttributeManager {

    /**
     * @see AttributeManager#getAttributes(Principal, List)
     */
    public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys) {
        return new HashMap<String, Object>();
    }

    @Override
    public String toString() {
        return EmptyAttributeManager.class.getName();
    }
}
