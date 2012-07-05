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
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.w3c.dom.Document;

/**
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@Entity
public class SAMLAssertionToken {

    @Id
    private String tokenId;
    
    @Column
    private Date creationDate = Calendar.getInstance().getTime();

    @Lob
    private byte[] token;

    /**
     * <p>
     * Default constructor.
     * </p>
     */
    public SAMLAssertionToken() {
    }

    public SAMLAssertionToken(String tokenId, AssertionType token) {
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
     * @return
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * @param creationDate
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    private void marshallAndSetToken(Object token) {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            
            new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(byteArray)).write((AssertionType) token);
            
            this.token = byteArray.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error marshalling token.", e);
        }
    }

    public Object unmarshalToken() {
        try {
            Document samlResponseDocument = DocumentUtil.getDocument(new ByteArrayInputStream(getToken()));

            SAMLParser samlParser = new SAMLParser();

            InputStream responseStream = DocumentUtil.getNodeAsStream(samlResponseDocument);
            
            return (AssertionType) samlParser.parse(responseStream);
        } catch (Exception e) {
            throw new RuntimeException("Error unmarshalling token.", e);
        }
    }

    public void setToken(byte[] token) {
        this.token = token;
    }
    
    public byte[] getToken() {
        return token;
    }
    
    
}
