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
package org.picketlink.identity.federation.core.sts.registry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * <p>
 * {@code RevokedToken} is a simple JPA entity used by the {@code JPABasedRevocationRegistry} to persist the ids of the
 * revoked
 * security tokens.
 * </p>
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
@Entity
public class RevokedToken {

    @Column
    private String tokenType;

    @Id
    private String tokenId;

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    public RevokedToken() {
    }

    /**
     * <p>
     * Creates an instance of {@code RevokedToken} with the specified token type and token id.
     * </p>
     *
     * @param tokenType a {@code String} representing the token type.
     * @param tokenId a {@code String} representing the token id.
     */
    public RevokedToken(String tokenType, String tokenId) {
        this.tokenType = tokenType;
        this.tokenId = tokenId;
    }

    /**
     * <p>
     * Obtains the type of the revoked security token.
     * </p>
     *
     * @return a {@code String} containing the revoked token type.
     */
    public String getTokenType() {
        return this.tokenType;
    }

    /**
     * <p>
     * Sets the type of revoked security token.
     * </p>
     *
     * @param tokenType a {@code String} containing the type to be set.
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * <p>
     * Obtains the id of the revoked security token.
     * </p>
     *
     * @return a {@code String} containing the revoked token id.
     */
    public String getTokenId() {
        return this.tokenId;
    }

    /**
     * <p>
     * Sets the id of the revoked security token.
     * </p>
     *
     * @param tokenId a {@code String} containing the id to be set.
     */
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

}
