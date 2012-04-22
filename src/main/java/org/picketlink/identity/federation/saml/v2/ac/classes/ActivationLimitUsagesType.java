package org.picketlink.identity.federation.saml.v2.ac.classes;

import java.math.BigInteger;


/**
 * <p>Java class for ActivationLimitUsagesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ActivationLimitUsagesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="number" use="required" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 

public class ActivationLimitUsagesType 
{
    protected BigInteger number;
    
    public ActivationLimitUsagesType( BigInteger theNumber )
    {
       this.number = theNumber;
    }

    /**
     * Gets the value of the number property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumber() {
        return number;
    }

}