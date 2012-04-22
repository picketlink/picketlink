package org.picketlink.identity.federation.saml.v2.ac.classes;
 
/**
 * <p>Java class for RestrictedPasswordType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RestrictedPasswordType">
 *   &lt;complexContent>
 *     &lt;restriction base="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}PasswordType">
 *       &lt;sequence>
 *         &lt;element name="Length" type="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}RestrictedLengthType"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Generation" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:ac:classes:AuthenticatedTelephony}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="ExternalVerification" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */ 
public class RestrictedPasswordType
    extends PasswordType
{ 
}
