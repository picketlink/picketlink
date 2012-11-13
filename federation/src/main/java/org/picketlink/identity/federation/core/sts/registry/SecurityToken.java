/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.sts.registry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;

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
     * Unmarshall the <code>token</code> byte array to a {@link AssertionType} instance.
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
     * Marshals a {@link AssertionType} instance into a byte array.
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