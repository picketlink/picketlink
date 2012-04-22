package org.picketlink.identity.federation.core.wstrust.plugins.saml;

import java.util.Map;

import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;

/**
 * <p>
 * An interface used by {@code SAML20TokenProvider} to retrieve an environment specific attribute that will be
 *  inserted into the Assertion.
 * </p>
 * 
 * @author <a href="mailto:Babak@redhat.com">Babak Mozaffari</a>
 */
public interface SAML20TokenAttributeProvider
{
   /**
    * Sets properties on the Attribute Provider that may affect its behavior
    * 
    * @param properties A set of string properties, some or all of which might impact the provider's behavior
    */
   void setProperties( Map<String, String> properties );

   /**
    * Given the security context, environment or other static or non-static criteria, returns an attribute statement
    *  to be included in the SAML v2 Assertion
    * 
    * @return An Attribute Statement to be inserted in the SAML v2 Assertion
    */
   AttributeStatementType getAttributeStatement();
}
