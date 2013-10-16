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
package org.picketlink.identity.federation.core.wstrust.handlers;

import javax.xml.namespace.QName;

import static org.picketlink.common.constants.WSTrustConstants.SAML2_ASSERTION_NS;
import static org.picketlink.common.constants.WSTrustConstants.WSSE_NS;

/**
 * A concrete implementation of {@link STSSecurityHandler} that can handle SAML version 2.0 Assertion inside of
 * {@link WSTrustConstants#WSSE_NS} elements.
 * <p/>
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class STSSaml20Handler extends STSSecurityHandler {

    /**
     * Qualified name for WSSE Security Header ({@link WSTrustConstants#WSSE_NS}:"Security")
     */
    public static final QName SECURITY_QNAME = new QName(WSSE_NS, "Security");

    /**
     * Qualified name for SAML Version 2.0 ({@link WSTrustConstants#SAML2_ASSERTION_NS}:"Assertion")
     */
    public static final QName SAML_TOKEN_QNAME = new QName(SAML2_ASSERTION_NS, "Assertion");

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.api.wstrust.handlers.PicketLinkSTSSecurityHandler#getSecurityElementQName()
     */
    @Override
    public QName getSecurityElementQName() {
        return SECURITY_QNAME;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.api.wstrust.handlers.PicketLinkSTSSecurityHandler#getTokenElementQName()
     */
    @Override
    public QName getTokenElementQName() {
        return SAML_TOKEN_QNAME;
    }

}
