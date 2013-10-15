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
package org.picketlink.identity.federation.core.wstrust;

import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenType;

/**
 * A Security Token that is based on SAML2
 *
 * @author Anil.Saldhana@redhat.com
 * @since Oct 12, 2009
 */
public class SAML2SecurityToken implements SAML2Object {

    private RequestSecurityTokenType token;

    public SAML2SecurityToken(RequestSecurityTokenType token) {
        this.token = token;
    }

    public RequestSecurityTokenType getToken() {
        return token;
    }
}