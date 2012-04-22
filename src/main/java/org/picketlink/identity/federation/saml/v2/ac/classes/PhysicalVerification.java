package org.picketlink.identity.federation.saml.v2.ac.classes;
 
/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="credentialLevel">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="primary"/>
 *             &lt;enumeration value="secondary"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class PhysicalVerification {

    protected String credentialLevel;

    /**
     * Gets the value of the credentialLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCredentialLevel() {
        return credentialLevel;
    }

    /**
     * Sets the value of the credentialLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCredentialLevel(String value) {
        this.credentialLevel = value;
    }

}
