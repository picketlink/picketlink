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
package org.picketlink.identity.federation.core.config;

/**
 * Aspects involved in trust decisions such as the domains that the IDP or the Service Provider trusts.
 *
 * <p>
 * Java class for TrustType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="TrustType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Domains" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class TrustType {

    protected String domains;

    /**
     * Gets the value of the domains property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getDomains() {
        return domains;
    }

    /**
     * Sets the value of the domains property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setDomains(String value) {
        this.domains = value;
    }

    /**
     * <p>Adds a new domain to the list of trusted domains.</p>
     * 
     * @param domain
     */
    public void addDomain(String domain) {
        if (this.domains == null) {
            this.domains = "";
        }
        
        if (!this.domains.isEmpty()) {
            domain = "," + domain;            
        }
        
        this.domains = this.domains + domain;
    }

}
