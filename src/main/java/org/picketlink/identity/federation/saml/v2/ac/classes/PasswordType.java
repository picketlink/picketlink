package org.picketlink.identity.federation.saml.v2.ac.classes;


/**
 * <p>Java class for PasswordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PasswordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Length" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Alphabet" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Generation" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ExternalVerification" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class PasswordType extends ExtensionListType {

    protected LengthType length;
    protected AlphabetType alphabet;
    protected Generation generation; 
    protected String externalVerification;

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link LengthType }
     *     
     */
    public LengthType getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthType }
     *     
     */
    public void setLength(LengthType value) {
        this.length = value;
    }

    /**
     * Gets the value of the alphabet property.
     * 
     * @return
     *     possible object is
     *     {@link AlphabetType }
     *     
     */
    public AlphabetType getAlphabet() {
        return alphabet;
    }

    /**
     * Sets the value of the alphabet property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlphabetType }
     *     
     */
    public void setAlphabet(AlphabetType value) {
        this.alphabet = value;
    }

    /**
     * Gets the value of the generation property.
     * 
     * @return
     *     possible object is
     *     {@link Generation }
     *     
     */
    public Generation getGeneration() {
        return generation;
    }

    /**
     * Sets the value of the generation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Generation }
     *     
     */
    public void setGeneration(Generation value) {
        this.generation = value;
    }

    /**
     * Gets the value of the externalVerification property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalVerification() {
        return externalVerification;
    }

    /**
     * Sets the value of the externalVerification property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalVerification(String value) {
        this.externalVerification = value;
    }

}
