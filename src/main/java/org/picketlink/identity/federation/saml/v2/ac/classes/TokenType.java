package org.picketlink.identity.federation.saml.v2.ac.classes;



/**
 * <p>Java class for TokenType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TokenType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}TimeSyncToken"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class TokenType extends ExtensionListType {
 
    protected TimeSyncTokenType timeSyncToken;  

    /**
     * Gets the value of the timeSyncToken property.
     * 
     * @return
     *     possible object is
     *     {@link TimeSyncTokenType }
     *     
     */
    public TimeSyncTokenType getTimeSyncToken() {
        return timeSyncToken;
    }

    /**
     * Sets the value of the timeSyncToken property.
     * 
     * @param value
     *     allowed object is
     *     {@link TimeSyncTokenType }
     *     
     */
    public void setTimeSyncToken(TimeSyncTokenType value) {
        this.timeSyncToken = value;
    }

}