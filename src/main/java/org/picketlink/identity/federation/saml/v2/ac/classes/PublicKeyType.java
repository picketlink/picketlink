package org.picketlink.identity.federation.saml.v2.ac.classes;

 

/**
 * <p>Java class for PublicKeyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PublicKeyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="keyValidation" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class PublicKeyType extends ExtensionListType {
    protected String keyValidation;

    /**
     * Gets the value of the keyValidation property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getKeyValidation() {
        return keyValidation;
    }

    /**
     * Sets the value of the keyValidation property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setKeyValidation(String value) {
        this.keyValidation = value;
    }
}