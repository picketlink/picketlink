package org.picketlink.identity.federation.saml.v2.ac.classes;

/**
 * <p>Java class for GoverningAgreementRefType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GoverningAgreementRefType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="governingAgreementRef" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class GoverningAgreementRefType {

    protected String governingAgreementRef;

    /**
     * Gets the value of the governingAgreementRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoverningAgreementRef() {
        return governingAgreementRef;
    }

    /**
     * Sets the value of the governingAgreementRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoverningAgreementRef(String value) {
        this.governingAgreementRef = value;
    }

}