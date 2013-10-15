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

import org.w3c.dom.Element;

/**
 * <p>
 * Standard implementation of the {@code SecurityToken} interface. This implementation stores the issued token as an
 * {@code Element}. The token providers are responsible for marshaling the security token into an {@code Element}
 * instance
 * because the security token marshaling process falls out of the scope of the STS (the STS only deals with WS-Trust
 * classes and
 * doesn't know how to marshal each specific token type).
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class StandardSecurityToken implements SecurityToken {

    private final String tokenType;

    private final String tokenId;

    private final Element token;

    /**
     * <p>
     * Creates an instance of {@code StandardSecurityToken} with the specified parameters.
     * </p>
     *
     * @param tokenType a {@code String} representing the type of the security token. This is usually the same type as
     * specified
     * in the WS-Trust request message.
     * @param token the security token in its {@code Element} form (i.e. the marshaled security token).
     * @param tokenID a {@code String} representing the id of the security token.
     */
    public StandardSecurityToken(String tokenType, Element token, String tokenID) {
        this.tokenType = tokenType;
        this.tokenId = tokenID;
        this.token = token;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityToken#getTokenType()
     */
    public String getTokenType() {
        return this.tokenType;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityToken#getTokenValue()
     */
    public Object getTokenValue() {
        return this.token;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.picketlink.identity.federation.core.wstrust.SecurityToken#getTokenID()
     */
    public String getTokenID() {
        return this.tokenId;
    }
}
