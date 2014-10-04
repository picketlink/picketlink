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
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2AttributeManager;
import org.picketlink.identity.federation.core.saml.v2.util.StatementUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An attribute manager that delegates to another manager for attributes
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 31, 2009
 */
public class DelegatedAttributeManager implements SAML2AttributeManager {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private final List<String> attributeKeys = new ArrayList<String>();
    private final AttributeManager delegate;

    public DelegatedAttributeManager(AttributeManager delegate, List<String> attributeKeys) {
        if (delegate != null) {
            this.delegate = delegate;
        } else {
            this.delegate = new EmptyAttributeManager();
        }

        if (attributeKeys != null) {
            this.attributeKeys.addAll(attributeKeys);
        }
    }

    /**
     * @see AttributeManager#getAttributes(Principal, List)
     */
    public Map<String, Object> getAttributes(Principal userPrincipal, List<String> attributeKeys) {
        if (delegate == null)
            throw logger.injectedValueMissing("Delegate");

        return delegate.getAttributes(userPrincipal, attributeKeys);
    }

    /**
     * @see AttributeManager#getAttributes(Principal, List)
     */
    public Map<String, Object> getAttributesMap(AuthnRequestType authnRequestType, Principal userPrincipal) {
        if (delegate == null)
            throw logger.injectedValueMissing("Delegate");

        Set<AttributeStatementType> attributeStatementTypes = getAttributes(authnRequestType, userPrincipal);
        Map<String, Object> attrMap;

        if (attributeStatementTypes != null && !attributeStatementTypes.isEmpty()) {
            attrMap = StatementUtil.asMap(attributeStatementTypes);
        } else {
            attrMap = delegate.getAttributes(userPrincipal, attributeKeys);
        }

        return attrMap;
    }

    @Override
    public Set<AttributeStatementType> getAttributes(AuthnRequestType authnRequestType, Principal userPrincipal) {
        if (delegate == null)
            throw logger.injectedValueMissing("Delegate");

        Set<AttributeStatementType> attributeStatementTypes = new HashSet<AttributeStatementType>();

        if (SAML2AttributeManager.class.isInstance(this.delegate)) {
            SAML2AttributeManager saml2AttributeManager = (SAML2AttributeManager) this.delegate;
            attributeStatementTypes.addAll(saml2AttributeManager.getAttributes(authnRequestType, userPrincipal));
        } else {
            Map<String, Object> attributes = getAttributes(userPrincipal, this.attributeKeys);

            if (attributes != null) {
                attributeStatementTypes.add(StatementUtil.createAttributeStatement(attributes));
            }
        }

        return attributeStatementTypes;
    }
}