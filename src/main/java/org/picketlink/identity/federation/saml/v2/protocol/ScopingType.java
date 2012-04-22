/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.saml.v2.protocol;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Java class for ScopingType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ScopingType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}IDPList" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}RequesterID" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ProxyCount" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class ScopingType 
{
    protected IDPListType idpList; 
    protected List<URI> requesterID = new ArrayList<URI>();
    
    protected BigInteger proxyCount;

    /**
     * Gets the value of the idpList property.
     * 
     * @return
     *     possible object is
     *     {@link IDPListType }
     *     
     */
    public IDPListType getIDPList() {
        return idpList;
    }

    /**
     * Sets the value of the idpList property.
     * 
     * @param value
     *     allowed object is
     *     {@link IDPListType }
     *     
     */
    public void setIDPList(IDPListType value) {
        this.idpList = value;
    }

    /**
     * Gets the value of the requesterID property.
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequesterID().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<URI> getRequesterID() 
    {
        return Collections.unmodifiableList( this.requesterID );
    }
    
    /**
     * Add requester id
     * @param uri
     */
    public void addRequesterID( URI uri )
    {
       this.requesterID.add( uri );
    }
    
    /**
     * Remove requester id
     * @param uri
     */
    public void removeRequesterID( URI uri )
    {
       this.requesterID.remove( uri );
    }

    /**
     * Gets the value of the proxyCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getProxyCount() {
        return proxyCount;
    }

    /**
     * Sets the value of the proxyCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setProxyCount(BigInteger value) {
        this.proxyCount = value;
    }

}
