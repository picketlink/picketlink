package org.picketlink.identity.federation.bindings.jboss.auth;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAML20TokenAttributeProvider;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;

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
public abstract class SAML20CommonTokenRoleAttributeProvider implements SAML20TokenAttributeProvider
{
   private static Logger logger = Logger.getLogger(SAML20CommonTokenRoleAttributeProvider.class);

   /**
    * The name of the principal in JBoss that is expected to include user roles
    */
   public static final String JBOSS_ROLE_PRINCIPAL_NAME = "Roles";

   /**
    * The default attribute name in the SAML Token that will carry the user's roles, if not configured otherwise
    */
   public static final String DEFAULT_TOKEN_ROLE_ATTRIBUTE_NAME = "role";

   /**
    * The name of the attribute in the SAML Token that will carry the user's roles
    */
   private String tokenRoleAttributeName;

   public void setProperties(Map<String, String> properties)
   {
      String roleAttrKey = this.getClass().getName() + ".tokenRoleAttributeName";
      tokenRoleAttributeName = properties.get(roleAttrKey);
      if (tokenRoleAttributeName == null)
      {
         tokenRoleAttributeName = DEFAULT_TOKEN_ROLE_ATTRIBUTE_NAME;
      }
   }

   public AttributeStatementType getAttributeStatement()
   {
      Subject subject = SecurityActions.getSecurityContext().getSubjectInfo().getAuthenticatedSubject();
      if (subject == null)
      {
         if (logger.isDebugEnabled())
            logger.debug("No authentication Subject found, cannot provide any user roles!");
         return null;
      }
      else
      {
         AttributeStatementType attributeStatement = new AttributeStatementType();
         AttributeType rolesAttribute = new AttributeType(tokenRoleAttributeName);
         attributeStatement.addAttribute(new ASTChoiceType(rolesAttribute));

         //List<Object> roles = rolesAttribute.getAttributeValue();
         for (Principal rolePrincipal : subject.getPrincipals())
         {
            if (JBOSS_ROLE_PRINCIPAL_NAME.equalsIgnoreCase(rolePrincipal.getName()))
            {
               Group simpleGroup = (Group) rolePrincipal;
               Enumeration<? extends Principal> members = simpleGroup.members();
               while (members.hasMoreElements())
               {
                  Principal role = members.nextElement();
                  rolesAttribute.addAttributeValue(role.getName());
                  //roles.add( role.getName() );
               }
            }
         }
         if (logger.isDebugEnabled())
            logger.debug("Returning an AttributeStatement with a [" + tokenRoleAttributeName
                  + "] attribute containing: " + rolesAttribute.getAttributeValue());
         return attributeStatement;
      }
   }
}