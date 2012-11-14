package org.picketlink.identity.federation.saml.v2.ac.classes;

/**
 * <p>
 * Java class for AuthnMethodBaseType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AuthnMethodBaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}PrincipalAuthenticationMechanism" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Authenticator" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}AuthenticatorTransportProtocol" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
public class OriginalAuthnMethodBaseType extends ExtensionListType {
    protected PrincipalAuthenticationMechanismType principalAuthenticationMechanism;
    protected AuthenticatorBaseType authenticator;
    protected AuthenticatorTransportProtocolType authenticatorTransportProtocol;

    /**
     * Gets the value of the principalAuthenticationMechanism property.
     *
     * @return possible object is {@link PrincipalAuthenticationMechanismType }
     *
     */
    public PrincipalAuthenticationMechanismType getPrincipalAuthenticationMechanism() {
        return principalAuthenticationMechanism;
    }

    /**
     * Sets the value of the principalAuthenticationMechanism property.
     *
     * @param value allowed object is {@link PrincipalAuthenticationMechanismType }
     *
     */
    public void setPrincipalAuthenticationMechanism(PrincipalAuthenticationMechanismType value) {
        this.principalAuthenticationMechanism = value;
    }

    /**
     * Gets the value of the authenticator property.
     *
     * @return possible object is {@link AuthenticatorBaseType }
     *
     */
    public AuthenticatorBaseType getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the value of the authenticator property.
     *
     * @param value allowed object is {@link AuthenticatorBaseType }
     *
     */
    public void setAuthenticator(AuthenticatorBaseType value) {
        this.authenticator = value;
    }

    /**
     * Gets the value of the authenticatorTransportProtocol property.
     *
     * @return possible object is {@link AuthenticatorTransportProtocolType }
     *
     */
    public AuthenticatorTransportProtocolType getAuthenticatorTransportProtocol() {
        return authenticatorTransportProtocol;
    }

    /**
     * Sets the value of the authenticatorTransportProtocol property.
     *
     * @param value allowed object is {@link AuthenticatorTransportProtocolType }
     *
     */
    public void setAuthenticatorTransportProtocol(AuthenticatorTransportProtocolType value) {
        this.authenticatorTransportProtocol = value;
    }

}