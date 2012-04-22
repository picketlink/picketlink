package org.picketlink.identity.federation.saml.v2.ac.classes;

 


/**
 * <p>Java class for SecretKeyProtectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SecretKeyProtectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}KeyActivation" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}KeyStorage" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class SecretKeyProtectionType extends ExtensionListType {

    protected KeyActivationType keyActivation;
    protected KeyStorageType keyStorage; 
    /**
     * Gets the value of the keyActivation property.
     * 
     * @return
     *     possible object is
     *     {@link KeyActivationType }
     *     
     */
    public KeyActivationType getKeyActivation() {
        return keyActivation;
    }

    /**
     * Sets the value of the keyActivation property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyActivationType }
     *     
     */
    public void setKeyActivation(KeyActivationType value) {
        this.keyActivation = value;
    }

    /**
     * Gets the value of the keyStorage property.
     * 
     * @return
     *     possible object is
     *     {@link KeyStorageType }
     *     
     */
    public KeyStorageType getKeyStorage() {
        return keyStorage;
    }

    /**
     * Sets the value of the keyStorage property.
     * 
     * @param value
     *     allowed object is
     *     {@link KeyStorageType }
     *     
     */
    public void setKeyStorage(KeyStorageType value) {
        this.keyStorage = value;
    }
}