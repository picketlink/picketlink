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

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.identity.federation.core.interfaces.AttributeManager;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * An attribute manager that delegates to another manager for attributes
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 31, 2009
 */
public class DelegatedAttributeManager implements AttributeManager {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private AttributeManager delegate = new EmptyAttributeManager();

    public DelegatedAttributeManager() {
    }

    /**
     * Set the delegate
     *
     * @param manager
     */
    public void setDelegate(AttributeManager manager) {
        this.delegate = manager;
    }

    /**
     * Is the delegate set?
     *
     * @return
     */
    public boolean isDelegateSet() {
        return this.delegate != null;
    }

    /**
     * @see AttributeManager#getAttributes(Principal, List)
     */
    public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys) {
        if (delegate == null)
            throw logger.injectedValueMissing("Delegate");
        return delegate.getAttributes(userPrincipal, attributeKeys);
    }
}