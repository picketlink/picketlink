package org.picketlink.identity.federation.saml.v2.ac.classes;


/**
 * <p>Java class for KeyActivationType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KeyActivationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}ActivationPin" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class KeyActivationType extends ExtensionListType
{
    protected ActivationPinType activationPin; 
    
    /**
     * Gets the value of the activationPin property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationPinType }
     *     
     */
    public ActivationPinType getActivationPin() {
        return activationPin;
    }

    /**
     * Sets the value of the activationPin property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationPinType }
     *     
     */
    public void setActivationPin(ActivationPinType value) {
        this.activationPin = value;
    }
 
}