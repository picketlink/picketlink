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
package org.picketlink.identity.federation.core.config;

import org.picketlink.identity.federation.web.constants.GeneralConstants;

/**
 * Service Provider Type
 *
 * <p>
 * Java class for SPType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="SPType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:picketlink:identity-federation:config:1.0}ProviderType">
 *       &lt;sequence>
 *         &lt;element name="ServiceURL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class SPType extends ProviderType {
    protected String serviceURL;

    protected String idpMetadataFile;

    /**
     * HTTP Redirect or HTTP Post Binding
     */
    protected String bindingType = "POST";

    /**
     * User can configure a relay state
     */
    protected String relayState;

    /**
     * User can configure an error page where the SP will redirect on encountering errors.
     */
    protected String errorPage = GeneralConstants.ERROR_PAGE_NAME;

    /**
     * Is the IDP sending POST binding request/response
     */
    protected boolean idpUsesPostBinding = true;

    private String logOutPage = GeneralConstants.LOGOUT_PAGE_NAME;

    /**
     * Gets the value of the serviceURL property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getServiceURL() {
        return serviceURL;
    }

    /**
     * Sets the value of the serviceURL property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setServiceURL(String value) {
        this.serviceURL = value;
    }

    /**
     * Get the IDP metadata file String
     *
     * @return
     */
    public String getIdpMetadataFile() {
        return idpMetadataFile;
    }

    /**
     * Set the IDP Metadata file String
     *
     * @param idpMetadataFile
     */
    public void setIdpMetadataFile(String idpMetadataFile) {
        this.idpMetadataFile = idpMetadataFile;
    }

    public String getBindingType() {
        return bindingType;
    }

    public void setBindingType(String bindingType) {
        if (bindingType.equals("POST") || bindingType.equals("REDIRECT")) {
            this.bindingType = bindingType;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public String getRelayState() {
        return relayState;
    }

    public void setRelayState(String relayState) {
        this.relayState = relayState;
    }

    public String getErrorPage() {
        return errorPage;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }

    public boolean isIdpUsesPostBinding() {
        return idpUsesPostBinding;
    }

    public void setIdpUsesPostBinding(boolean idpPostBinding) {
        this.idpUsesPostBinding = idpPostBinding;
    }

    public String getLogOutPage() {
        return this.logOutPage;
    }
    
    public void setLogOutPage(String logOutPage) {
        this.logOutPage = logOutPage;
    }
}