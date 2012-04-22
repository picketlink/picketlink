package org.picketlink.identity.federation.saml.v2.ac.classes;

/**
 * <p>Java class for ActivationLimitType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivationLimitType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}ActivationLimitDuration"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}ActivationLimitUsages"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}ActivationLimitSession"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class ActivationLimitType {
 
    protected ActivationLimitDurationType activationLimitDuration; 
    protected ActivationLimitUsagesType activationLimitUsages; 
    protected ActivationLimitSessionType activationLimitSession;

    /**
     * Gets the value of the activationLimitDuration property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationLimitDurationType }
     *     
     */
    public ActivationLimitDurationType getActivationLimitDuration() {
        return activationLimitDuration;
    }

    /**
     * Sets the value of the activationLimitDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationLimitDurationType }
     *     
     */
    public void setActivationLimitDuration(ActivationLimitDurationType value) {
        this.activationLimitDuration = value;
    }

    /**
     * Gets the value of the activationLimitUsages property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationLimitUsagesType }
     *     
     */
    public ActivationLimitUsagesType getActivationLimitUsages() {
        return activationLimitUsages;
    }

    /**
     * Sets the value of the activationLimitUsages property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationLimitUsagesType }
     *     
     */
    public void setActivationLimitUsages(ActivationLimitUsagesType value) {
        this.activationLimitUsages = value;
    }

    /**
     * Gets the value of the activationLimitSession property.
     * 
     * @return
     *     possible object is
     *     {@link ActivationLimitSessionType }
     *     
     */
    public ActivationLimitSessionType getActivationLimitSession() {
        return activationLimitSession;
    }

    /**
     * Sets the value of the activationLimitSession property.
     * 
     * @param value
     *     allowed object is
     *     {@link ActivationLimitSessionType }
     *     
     */
    public void setActivationLimitSession(ActivationLimitSessionType value) {
        this.activationLimitSession = value;
    }

}
