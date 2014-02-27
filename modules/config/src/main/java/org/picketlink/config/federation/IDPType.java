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
package org.picketlink.config.federation;

import org.picketlink.common.util.StringUtil;

/**
 * IDP Type defines the configuration for an Identity Provider.
 * <p/>
 * <p/>
 * <p/>
 * Java class for IDPType complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="IDPType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:picketlink:identity-federation:config:1.0}ProviderType">
 *       &lt;sequence>
 *         &lt;element name="Encryption" type="{urn:picketlink:identity-federation:config:1.0}EncryptionType"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="AssertionValidity" type="{http://www.w3.org/2001/XMLSchema}long" default="300000" />
 *       &lt;attribute name="RoleGenerator" type="{http://www.w3.org/2001/XMLSchema}string"
 * default="org.picketlink.identity.federation.bindings.tomcat.TomcatRoleGenerator" />
 *       &lt;attribute name="AttributeManager" type="{http://www.w3.org/2001/XMLSchema}string"
 * default="org.picketlink.identity.federation.bindings.tomcat.TomcatAttributeManager" />
 *       &lt;attribute name="Encrypt" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class IDPType extends ProviderType {

    protected EncryptionType encryption;

    protected String roleGenerator;

    protected String attributeManager;

    protected Boolean encrypt = Boolean.FALSE;

    protected String hostedURI;

    /**
     * SAML Web Browser SSO Profile has a requirement that the IDP does not respond back in Redirect Binding. Set this
     * to false if you want to force the IDP to respond to SPs using the Redirect Binding.
     */
    private boolean strictPostBinding = true;

    /**
     * Tells the IDP if SSL clients should be authenticated.
     */
    private boolean sslClientAuthentication = false;

    /**
     * If the user wants to set a particular {@link IdentityParticipantStack}
     */
    private String identityParticipantStack = null;

    /**
     * Gets the value of the encryption property.
     *
     * @return possible object is {@link EncryptionType }
     */
    public EncryptionType getEncryption() {
        return encryption;
    }

    /**
     * Sets the value of the encryption property.
     *
     * @param value allowed object is {@link EncryptionType }
     */
    public void setEncryption(EncryptionType value) {
        this.encryption = value;
    }

    /**
     * Gets the value of the roleGenerator property.
     *
     * @return possible object is {@link String }
     */
    public String getRoleGenerator() {
        if (roleGenerator == null) {
            return "org.picketlink.identity.federation.bindings.tomcat.TomcatRoleGenerator";
        } else {
            return roleGenerator;
        }
    }

    /**
     * Sets the value of the roleGenerator property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRoleGenerator(String value) {
        this.roleGenerator = value;
    }

    /**
     * Gets the value of the attributeManager property.
     *
     * @return possible object is {@link String }
     */
    public String getAttributeManager() {
        if (attributeManager == null) {
            return "org.picketlink.identity.federation.bindings.tomcat.TomcatAttributeManager";
        } else {
            return attributeManager;
        }
    }

    /**
     * Sets the value of the attributeManager property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAttributeManager(String value) {
        this.attributeManager = value;
    }

    /**
     * Gets the value of the encrypt property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isEncrypt() {
        if (encrypt == null) {
            return false;
        } else {
            return encrypt;
        }
    }

    /**
     * Sets the value of the encrypt property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setEncrypt(Boolean value) {
        this.encrypt = value;
    }

    /**
     * Gets the value of the encrypt property.
     *
     * @return possible object is {@link String}
     */
    public String getHostedURI() {
        return hostedURI;
    }

    /**
     * Sets the value of the hostedURI property.
     *
     * @param value allowed object is {@link String}
     */
    public void setHostedURI(String hostedURI) {
        this.hostedURI = hostedURI;
    }

    /**
     * Import values from another {@link IDPType}
     *
     * @param other
     */
    public void importFrom(IDPType other) {
        super.importFrom(other);

        String attributeManager = other.getAttributeManager();
        if (StringUtil.isNotNull(attributeManager)) {
            setAttributeManager(attributeManager);
        }

        encrypt = other.isEncrypt();

        if (StringUtil.isNotNull(other.getRoleGenerator())) {
            this.roleGenerator = other.getRoleGenerator();
        }
    }

    /**
     * Sets the value of the strictPostBinding property.
     *
     * @param value allowed object is {@link Boolean }
     */
    public void setStrictPostBinding(boolean strictPostBinding) {
        this.strictPostBinding = strictPostBinding;
    }

    /**
     * Gets the value of the strictPostBinding property.
     *
     * @return possible object is {@link Boolean }
     */
    public boolean isStrictPostBinding() {
        return this.strictPostBinding;
    }

    /**
     * Gets the value of the supportsSSL property.
     *
     * @return
     */
    public boolean isSSLClientAuthentication() {
        return this.sslClientAuthentication;
    }

    /**
     * Sets the value of the supportsSSL property.
     *
     * @param sslClientAuthentication
     */
    public void setSSLClientAuthentication(boolean sslClientAuthentication) {
        this.sslClientAuthentication = sslClientAuthentication;
    }

    /**
     * Sets the value of the identityParticipantStack property. The value must be the name of a {@link
     * IdentityParticipantStack} subclass.
     *
     * @param value allowed object is {@link String }
     */
    public void setIdentityParticipantStack(String identityParticipantStack) {
        this.identityParticipantStack = identityParticipantStack;
    }

    /**
     * Gets the value of the identityParticipantStack property.
     *
     * @return possible object is {@link String }
     */
    public String getIdentityParticipantStack() {
        return this.identityParticipantStack;
    }
}