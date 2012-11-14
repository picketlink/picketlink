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
package org.picketlink.identity.federation.ws.trust;

import org.picketlink.identity.federation.ws.wss.utility.AttributedDateTime;

/**
 * <p>
 * Java class for LifetimeType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="LifetimeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Created" minOccurs="0"/>
 *         &lt;element ref="{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Expires" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class LifetimeType {
    protected AttributedDateTime created;

    protected AttributedDateTime expires;

    /**
     * Gets the value of the created property.
     *
     * @return possible object is {@link AttributedDateTime }
     *
     */
    public AttributedDateTime getCreated() {
        return created;
    }

    /**
     * Sets the value of the created property.
     *
     * @param value allowed object is {@link AttributedDateTime }
     *
     */
    public void setCreated(AttributedDateTime value) {
        this.created = value;
    }

    /**
     * Gets the value of the expires property.
     *
     * @return possible object is {@link AttributedDateTime }
     *
     */
    public AttributedDateTime getExpires() {
        return expires;
    }

    /**
     * Sets the value of the expires property.
     *
     * @param value allowed object is {@link AttributedDateTime }
     *
     */
    public void setExpires(AttributedDateTime value) {
        this.expires = value;
    }
}