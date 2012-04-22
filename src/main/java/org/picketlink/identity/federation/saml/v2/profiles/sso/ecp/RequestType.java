package org.picketlink.identity.federation.saml.v2.profiles.sso.ecp;

import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.IDPListType;
 


/**
 * <p>Java class for RequestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Issuer"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:protocol}IDPList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://schemas.xmlsoap.org/soap/envelope/}mustUnderstand use="required""/>
 *       &lt;attribute ref="{http://schemas.xmlsoap.org/soap/envelope/}actor use="required""/>
 *       &lt;attribute name="ProviderName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="IsPassive" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class RequestType {

   protected NameIDType issuer;
    protected IDPListType idpList; 
    protected Boolean mustUnderstand; 
    protected String actor; 
    protected String providerName; 
    protected Boolean isPassive;

    /**
     * Gets the value of the issuer property.
     * 
     * @return
     *     possible object is
     *     {@link NameIDType }
     *     
     */
    public NameIDType getIssuer() {
        return issuer;
    }

    /**
     * Sets the value of the issuer property.
     * 
     * @param value
     *     allowed object is
     *     {@link NameIDType }
     *     
     */
    public void setIssuer(NameIDType value) {
        this.issuer = value;
    }

    /**
     * Gets the value of the idpList property.
     * 
     * @return
     *     possible object is
     *     {@link IDPListType }
     *     
     */
    public IDPListType getIDPList() {
        return idpList;
    }

    /**
     * Sets the value of the idpList property.
     * 
     * @param value
     *     allowed object is
     *     {@link IDPListType }
     *     
     */
    public void setIDPList(IDPListType value) {
        this.idpList = value;
    }

    /**
     * Gets the value of the mustUnderstand property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Boolean isMustUnderstand() {
        return mustUnderstand;
    }

    /**
     * Sets the value of the mustUnderstand property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMustUnderstand(Boolean value) {
        this.mustUnderstand = value;
    }

    /**
     * Gets the value of the actor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActor() {
        return actor;
    }

    /**
     * Sets the value of the actor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActor(String value) {
        this.actor = value;
    }

    /**
     * Gets the value of the providerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Sets the value of the providerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProviderName(String value) {
        this.providerName = value;
    }

    /**
     * Gets the value of the isPassive property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsPassive() {
        return isPassive;
    }

    /**
     * Sets the value of the isPassive property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsPassive(Boolean value) {
        this.isPassive = value;
    }

}
