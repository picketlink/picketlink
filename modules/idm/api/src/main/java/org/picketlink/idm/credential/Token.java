/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.idm.credential;

import org.picketlink.idm.credential.storage.TokenCredentialStorage;
import org.picketlink.idm.model.Account;

/**
 * <p>Represents a token credential.</p>
 *
 * <p>Basically, a token is a string representation for a specific user. Usually, the token should provide the following information:</p>
 *
 * <ul>
 *  <li>Token identifier</li>
 *  <li>Issuer</li>
 *  <li>Subject</li>
 *  <li>Attributes</li>
 * </ul>
 *
 * <p>
 *     The issuer can be used to know which partition or realm is associated with a token, allowing {@link org.picketlink.idm.credential.Token.Provider}
 *  implementations to find the {@link org.picketlink.idm.model.Account} associated with the token.
 * </p>
 *
 * <p>
 *     Each token type has its own {@link org.picketlink.idm.credential.Token.Provider}, which is responsible to provide important management operations.
 * </p>
 *
 * @author Pedro Igor
 *
 * @see org.picketlink.idm.credential.Token.Provider
 * @see org.picketlink.idm.credential.handler.TokenCredentialHandler
 */
public class Token {

    private final String type = getClass().getName();
    private final String token;

    public Token(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public String getToken() {
        return this.token;
    }

    /**
     * <p>
     *     Token providers are responsible to provide some importantant management operations for a specific {@link org.picketlink.idm.credential.Token} type.
     * </p>
     *
     * @author Pedor Igor
     */
    public interface Provider {

        /**
         * <p>
         *     Returns the {@link org.picketlink.idm.model.Account} associated with the given token.
         * </p>
         *
         * @param token
         * @return
         */
        Account getAccount(Token token);

        /**
         * <p>Creates an instance from the given <code>value</code>.</p>
         *
         * <p>If an instance can not be created from the given value, this method should return null.</p>
         *
         * @param value
         * @return
         */
        Token create(Object value);

        /**
         * <p>Issues a new token for the given {@link org.picketlink.idm.model.Account}.</p>
         *
         * @param account
         * @return
         */
        Token issue(Account account);

        /**
         * <p>
         *     Renew a token based on the current token in use.
         * </p>
         *
         * @param currentToken
         * @return
         */
        Token renew(Token currentToken);

        /**
         * <p>
         *     Validates a token.
         * </p>
         *
         * @param token
         * @return
         */
        boolean validate(Token token);

        /**
         * <p>Invalidates the current token for the given {@link org.picketlink.idm.model.Account}.</p>
         *
         * @param account
         */
        void invalidate(Account account);

        /**
         * <p>Indicates if this token provider supports the given {@link org.picketlink.idm.credential.Token} instance.</p>
         *
         * @param token
         * @return
         */
        boolean supports(Token token);

        /**
         * <p>
         *     Returns a {@link org.picketlink.idm.credential.storage.TokenCredentialStorage} instance that should be used to store
         *     the token supported by this provider.
         * </p>
         *
         * <p>
         *     Subclasses can extend this class in order to provide any additional state when storing tokens to an identity store.
         * </p>
         *
         * @param account
         * @param token
         * @param <T>
         * @return
         */
        <T extends TokenCredentialStorage> T getTokenStorage(Account account, Token token);
    }
}