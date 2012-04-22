package org.picketlink.identity.federation.saml.v2.ac.classes;

import java.math.BigInteger;



/**
 * <p>Java class for TimeSyncTokenType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TimeSyncTokenType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="DeviceType" use="required" type="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}DeviceTypeType" />
 *       &lt;attribute name="SeedLength" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *       &lt;attribute name="DeviceInHand" use="required" type="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}booleanType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class TimeSyncTokenType {
    protected DeviceTypeType deviceType; 
    protected BigInteger seedLength; 
    protected Boolean deviceInHand;

    /**
     * Gets the value of the deviceType property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceTypeType }
     *     
     */
    public DeviceTypeType getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the value of the deviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceTypeType }
     *     
     */
    public void setDeviceType(DeviceTypeType value) {
        this.deviceType = value;
    }

    /**
     * Gets the value of the seedLength property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getSeedLength() {
        return seedLength;
    }

    /**
     * Sets the value of the seedLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setSeedLength(BigInteger value) {
        this.seedLength = value;
    }

    /**
     * Gets the value of the deviceInHand property.
     * 
     * @return
     *     possible object is
     *     {@link BooleanType }
     *     
     */
    public Boolean getDeviceInHand() {
        return deviceInHand;
    }

    /**
     * Sets the value of the deviceInHand property.
     * 
     * @param value
     *     allowed object is
     *     {@link BooleanType }
     *     
     */
    public void setDeviceInHand(Boolean value) {
        this.deviceInHand = value;
    }
}