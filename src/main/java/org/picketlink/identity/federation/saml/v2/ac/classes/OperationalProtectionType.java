package org.picketlink.identity.federation.saml.v2.ac.classes;



/**
 * <p>Java class for OperationalProtectionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OperationalProtectionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}SecurityAudit" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}DeactivationCallCenter" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class OperationalProtectionType extends ExtensionListType {
 
    protected SecurityAuditType securityAudit; 
    protected ExtensionOnlyType deactivationCallCenter;  

    /**
     * Gets the value of the securityAudit property.
     * 
     * @return
     *     possible object is
     *     {@link SecurityAuditType }
     *     
     */
    public SecurityAuditType getSecurityAudit() {
        return securityAudit;
    }

    /**
     * Sets the value of the securityAudit property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecurityAuditType }
     *     
     */
    public void setSecurityAudit(SecurityAuditType value) {
        this.securityAudit = value;
    }

    /**
     * Gets the value of the deactivationCallCenter property.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionOnlyType }
     *     
     */
    public ExtensionOnlyType getDeactivationCallCenter() {
        return deactivationCallCenter;
    }

    /**
     * Sets the value of the deactivationCallCenter property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionOnlyType }
     *     
     */
    public void setDeactivationCallCenter(ExtensionOnlyType value) {
        this.deactivationCallCenter = value;
    } 
}