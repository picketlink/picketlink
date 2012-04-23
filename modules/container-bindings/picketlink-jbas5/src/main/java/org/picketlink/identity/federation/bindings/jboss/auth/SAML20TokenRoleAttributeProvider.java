package org.picketlink.identity.federation.bindings.jboss.auth;

import org.picketlink.identity.federation.bindings.jboss.auth.SAML20CommonTokenRoleAttributeProvider;

/**
 * <p>
 * An implementation of the SAML20TokenAttributeProvider for JBoss which looks at the authenticated Subject
 *  and creates an Attribute containing the user's roles.
 * </p>
 * 
 * <h3>Configuration</h3>
 * <pre>{@code
 *  <TokenProviders>
 *    <TokenProvider ProviderClass="org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenProvider"
 *        TokenType="http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0"
 *        TokenElement="Assertion"
 *        TokenElementNS="urn:oasis:names:tc:SAML:2.0:assertion">
 *      <Property Key="AttributeProvider" Value="org.picketlink.identity.federation.bindings.jboss.auth.SAML20TokenRoleAttributeProvider"/>
 *      <Property Key="org.picketlink.identity.federation.bindings.jboss.auth.SAML20TokenRoleAttributeProvider.tokenRoleAttributeName" Value="role"/>
 *    </TokenProvider>
 *  </TokenProviders>
 * }
 * </pre>
 * 
 * When configured, this attribute provider will be called by the {@code SAML20TokenProvider} to return an {@code AttributeStatement}
 * from the STS token and supply them for insertion into the JAAS Subject.
 * This returns a multi-valued Attribute to be included in the Assertion, where each value of the attribute is a JBoss user role.
 * The name of this attribute defaults to {@code DEFAULT_TOKEN_ROLE_ATTRIBUTE_NAME} but
 * may be set to any value through an optional property as shown above.
 * 
 * @author <a href="mailto:Babak@redhat.com">Babak Mozaffari</a>
 */
public class SAML20TokenRoleAttributeProvider extends SAML20CommonTokenRoleAttributeProvider
{
}