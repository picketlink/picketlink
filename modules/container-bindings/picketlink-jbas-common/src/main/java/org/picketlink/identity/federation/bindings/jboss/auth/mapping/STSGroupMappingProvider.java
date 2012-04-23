package org.picketlink.identity.federation.bindings.jboss.auth.mapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jboss.security.identity.RoleGroup;
import org.jboss.security.identity.plugins.SimpleRole;
import org.jboss.security.identity.plugins.SimpleRoleGroup;
import org.jboss.security.mapping.MappingProvider;
import org.jboss.security.mapping.MappingResult;
import org.picketlink.identity.federation.bindings.jboss.auth.SAML20CommonTokenRoleAttributeProvider;
import org.picketlink.identity.federation.core.wstrust.auth.AbstractSTSLoginModule;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.assertion.StatementAbstractType;
import org.w3c.dom.Element;

/**
 * <p>
 * This mapping provider looks at the role attributes in the Assertion and
 *  returns corresponding JBoss RoleGroup objects for insertion into the Subject.
 * </p>
 * 
 * <h3>Configuration</h3>
 * <pre>{@code
 * <application-policy name="saml-issue-token">
 *   <authentication>
 *     <login-module code="org.picketlink.identity.federation.core.wstrust.auth.STSIssuingLoginModule" flag="required">
 *       <module-option name="configFile">/sts-client.properties</module-option>
 *       <module-option name="password-stacking">useFirstPass</module-option>
 *     </login-module>
 *   </authentication>
 *   <mapping>
 *     <mapping-module code="org.picketlink.identity.federation.bindings.jboss.auth.mapping.STSPrincipalMappingProvider" type="principal"/>
 *     <mapping-module code="org.picketlink.identity.federation.bindings.jboss.auth.mapping.STSGroupMappingProvider" type="role">
 *       <module-option name="token-role-attribute-name">role</module-option>
 *     </mapping-module>
 *   </mapping>
 * </application-policy>
 * }
 * </pre>
 * 
 * As demonstrated above, this mapping provider is typically configured for an STS Login Module to extract user roles
 * from the STS token and supply them for insertion into the JAAS Subject.
 * 
 * This mapping provider looks for a multi-valued Attribute in the Assertion, where each value is a user role.
 * The name of this attribute defaults to {@code SAML20TokenRoleAttributeProvider.DEFAULT_TOKEN_ROLE_ATTRIBUTE_NAME} but
 * may be set to any value through the "token-role-attribute-name" module option.
 * <p/>
 * 
 * 
 * @author <a href="mailto:Babak@redhat.com">Babak Mozaffari</a>
 */
public class STSGroupMappingProvider implements MappingProvider<RoleGroup>
{
   private Logger log = Logger.getLogger(STSGroupMappingProvider.class);

   private MappingResult<RoleGroup> result;

   private String tokenRoleAttributeName;

   public void init(Map<String, Object> contextMap)
   {
      Object tokenRoleAttributeObject = contextMap.get("token-role-attribute-name");
      if (tokenRoleAttributeObject != null)
      {
         tokenRoleAttributeName = (String) tokenRoleAttributeObject;
      }
      else
      {
         tokenRoleAttributeName = SAML20CommonTokenRoleAttributeProvider.DEFAULT_TOKEN_ROLE_ATTRIBUTE_NAME;
      }

      //No initialization needed
      if (log.isDebugEnabled())
      {
         log.debug("Initialized with " + contextMap);
      }
   }

   public void performMapping(Map<String, Object> contextMap, RoleGroup Group)
   {
      if (log.isDebugEnabled())
      {
         log.debug("performMapping with map as " + contextMap);
      }
      if (contextMap == null)
      {
         log.warn("Empty context map. SAML Token must be provided in the context map to extract a Principal");
      }

      Object tokenObject = contextMap.get(AbstractSTSLoginModule.SHARED_TOKEN);
      if (!(tokenObject instanceof Element))
      {
         //With Tomcat SSO Valves, mapping providers DO get called automatically, so there may be no tokens and errors should be expected and handled
         log.warn("Did not find a token " + Element.class.getName() + " under " + AbstractSTSLoginModule.SHARED_TOKEN
               + " in the map");
      }

      try
      {
         Element tokenElement = (Element) tokenObject;
         AssertionType assertion = SAMLUtil.fromElement(tokenElement);

         // check the assertion statements and look for role attributes.
         AttributeStatementType attributeStatement = this.getAttributeStatement(assertion);
         if (attributeStatement != null)
         {
            RoleGroup rolesGroup = new SimpleRoleGroup(SAML20CommonTokenRoleAttributeProvider.JBOSS_ROLE_PRINCIPAL_NAME);
            List<ASTChoiceType> attributeList = attributeStatement.getAttributes();
            for ( ASTChoiceType obj : attributeList)
            {
               AttributeType attribute = obj.getAttribute();
               if( attribute != null )
               {
               // if this is a role attribute, get its values and add them to the role set.
                  if (tokenRoleAttributeName.equals(attribute.getName()))
                  {
                     for (Object value : attribute.getAttributeValue())
                     {
                        rolesGroup.addRole(new SimpleRole((String) value));
                     }
                  }
               }
            }
            result.setMappedObject(rolesGroup);
            if (log.isDebugEnabled())
            {
               log.debug("Mapped roles to " + rolesGroup);
            }
         }
      }
      catch ( Exception e )
      {
         log.error("Failed to parse token", e);
      }
   }

   public void setMappingResult(MappingResult<RoleGroup> mappingResult)
   {
      this.result = mappingResult;
   }

   /**
    * @see MappingProvider#supports(Class)
    */
   public boolean supports(Class<?> p)
   {
      if (RoleGroup.class.isAssignableFrom(p))
         return true;

      return false;
   }

   /**
    * <p>
    * Checks if the specified SAML assertion contains a {@code AttributeStatementType} and returns this type when it
    * is available.
    * </p>
    * 
    * @param assertion a reference to the {@code AssertionType} that may contain an {@code AttributeStatementType}.
    * @return the assertion's {@code AttributeStatementType}, or {@code null} if no such type can be found in the SAML
    * assertion.
    */
   private AttributeStatementType getAttributeStatement(AssertionType assertion)
   {
      Set<StatementAbstractType> statementList = assertion.getStatements();
      if (statementList.size() != 0)
      {
         for (StatementAbstractType statement : statementList)
         {
            if (statement instanceof AttributeStatementType)
               return (AttributeStatementType) statement;
         }
      }
      return null;
   }
}