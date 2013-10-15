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
import javax.persistence.Lob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 * {@code SecurityToken} is a simple JPA entity used by the {@code JPABasedTokenRegistry} to persist tokens.
 * </p>
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@Entity
public class SecurityToken {

    @Id
    private String tokenId;

    @Column
    private Date tokenCreationDate = Calendar.getInstance().getTime();

    @Lob
    private byte[] token;

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    public SecurityToken() {
    }

    public SecurityToken(String tokenId, Object token) {
        this.tokenId = tokenId;
        marshallAndSetToken(token);
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

    /**
     * <p>Gets the {@link Date} which this token was created.</p>
     *
     * @return
     */
    public Date getTokenCreationDate() {
        return this.tokenCreationDate;
    }

    /**
     * <p>Sets the {@link Date} which this token was created.</p>
     *
     * @param tokenCreationDate
     */
    public void setTokenCreationDate(Date tokenCreationDate) {
        this.tokenCreationDate = tokenCreationDate;
    }

    /**
     * <p>Sets the byte array representation of the token object.</p>
     *
     * @param token
     */
    public void setToken(byte[] token) {
        this.token = token;
    }

    /**
     * <p>Gets the byte array representation of the token object.</p>
     *
     * @return
     */
    public byte[] getToken() {
        return token;
    }

    /**
     * <p>
     * Unmarshall the <code>token</code> byte array to a {@link org.picketlink.identity.federation.saml.v2.assertion.AssertionType}
     * instance.
     * </p>
     *
     * @return
     */
    public Object unmarshalToken() {
        try {
            ByteArrayInputStream byteArray = new ByteArrayInputStream(getToken());

            return new ObjectInputStream(byteArray).readObject();
        } catch (Exception e) {
            throw new RuntimeException("Error unmarshalling token.", e);
        }
    }

    /**
     * <p>
     * Marshals a {@link org.picketlink.identity.federation.saml.v2.assertion.AssertionType} instance into a byte
     * array.
     * </p>
     *
     * @param token
     */
    private void marshallAndSetToken(Object token) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();

            new ObjectOutputStream(byteArray).writeObject(token);

            this.token = byteArray.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error marshalling token.", e);
        }
    }

}