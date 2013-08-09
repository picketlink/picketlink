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
package org.picketlink.config.federation;

import org.picketlink.common.constants.GeneralConstants;


/**
 * Service Provider Type
 * <p/>
 * <p/>
 * Java class for SPType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within this class.
 * <p/>
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
     * <p>The URL that should be used during a GLO logout. This would usually be an URL from the IDP.</p>
     */
    private String logoutUrl;

    /**
     * The URL used to send a response for an IDP logout request
     */
    private String logoutResponseLocation;

    /**
     * Gets the value of the serviceURL property.
     *
     * @return possible object is {@link String }
     */
    public String getServiceURL() {
        return serviceURL;
    }

    /**
     * Sets the value of the serviceURL property.
     *
     * @param value allowed object is {@link String }
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

    public String getLogoutUrl() {
        return this.logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getLogoutResponseLocation() {
        return logoutResponseLocation;
    }

    public void setLogoutResponseLocation(String logoutResponseLocation) {
        this.logoutResponseLocation = logoutResponseLocation;
    }
}