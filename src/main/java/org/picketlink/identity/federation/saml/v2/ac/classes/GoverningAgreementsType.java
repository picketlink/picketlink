package org.picketlink.identity.federation.saml.v2.ac.classes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * <p>Java class for GoverningAgreementsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GoverningAgreementsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}GoverningAgreementRef" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class GoverningAgreementsType {

    protected List<GoverningAgreementRefType> governingAgreementRef = new ArrayList<GoverningAgreementRefType>();

    public void add( GoverningAgreementRefType gov )
    {
       this.governingAgreementRef.add(gov);
    }
    
    public void remove( GoverningAgreementRefType gov )
    {
       this.governingAgreementRef.remove(gov);
    }
    /**
     * Gets the value of the governingAgreementRef property.
     *  
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GoverningAgreementRefType }
     * 
     * 
     */
    public List<GoverningAgreementRefType> getGoverningAgreementRef() {
        return Collections.unmodifiableList( this.governingAgreementRef );
    }

}
