package org.picketlink.identity.federation.saml.v2.ac.classes;
 


/**
 * <p>Java class for AuthnContextDeclarationBaseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthnContextDeclarationBaseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}AuthnContextDeclarationBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Identification" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}TechnicalProtection" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}OperationalProtection" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}AuthnMethod"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}GoverningAgreements" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ID" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class AuthnContextDeclarationBaseType
    extends OriginalAuthnContextDeclarationBaseType
{
}